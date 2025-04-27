package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.Setter;
import org.nevertouchgrass.prolific.components.ArrayListHolder;
import org.nevertouchgrass.prolific.javafxcontroller.settings.RunConfigFooterController;
import org.nevertouchgrass.prolific.javafxcontroller.settings.RunConfigSettingHeaderController;
import org.nevertouchgrass.prolific.javafxcontroller.settings.SettingsFooterController;
import org.nevertouchgrass.prolific.javafxcontroller.settings.contract.SettingsOption;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.configurations.RunConfigService;
import org.nevertouchgrass.prolific.service.configurations.creators.contract.RunConfigurationCreator;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public abstract class AbstractSettingsOption implements SettingsOption {

    protected static final String ERROR = "error";

    @Setter(onMethod_ = @Autowired)
    protected UserSettingsHolder userSettingsHolder;
    @Setter(onMethod_ = @Autowired)
    protected UserSettingsService userSettingsService;
    @Setter(onMethod_ = @Autowired)
    protected FxmlProvider fxmlProvider;
    @Setter(onMethod_ = @Autowired)
    protected SettingsFooterController settingsFooterController;
    @Setter(onMethod_ = @Autowired)
    protected LocalizationProvider localizationProvider;
    @Setter(onMethod_ = @Autowired)
    protected RunConfigFooterController runConfigFooterController;
    @Setter(onMethod_ = @Autowired)
    protected RunConfigSettingHeaderController runConfigSettingHeaderController;
    @Setter(onMethod_ = @Autowired)
    protected RunConfigService runConfigService;
    protected Stage stage;

    protected Map<Node, StringProperty> pathChooserLocalizationMap = new HashMap<>();
    protected Map<Node, TextField> pathChooserPathSettingMap = new HashMap<>();
    protected List<TextField> textFields = new ArrayList<>();

    @FXML protected ArrayListHolder<Node> options;

    @Override
    public List<Node> getOptions() {
        return new ArrayList<>(options.getItems());
    }

    protected boolean checkProvidedPath(String path, TextField pathSetting, Label errorMessage) {
        try {
            var result = Files.exists(Paths.get(path));
            if (result) {
                pathSetting.getStyleClass().remove(ERROR);
                errorMessage.setVisible(false);
                errorMessage.setManaged(false);
            } else {
                if (!pathSetting.getStyleClass().contains(ERROR)) {
                    pathSetting.getStyleClass().add(ERROR);
                }
                errorMessage.setVisible(true);
                errorMessage.setManaged(true);
            }
            return result;
        } catch (Exception ignore) {
            return false;
        }
    }

    protected boolean checkProvidedNonEmptyString(@NonNull String text, TextField textField, Label errorMessage) {
        if (text.isBlank()) {
            if (!textField.getStyleClass().contains(ERROR)) {
                textField.getStyleClass().add(ERROR);
            }
            errorMessage.setVisible(true);
            errorMessage.setManaged(true);
            return false;
        }

        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
        textField.getStyleClass().remove(ERROR);

        return true;
    }

    protected TextFormatter.Change createIntegerChange(TextFormatter.Change change, int min, int max) {
        String newText = change.getControlNewText();
        if (newText.matches("\\d*")) {
            try {
                if (!newText.isEmpty()) {
                    var value = Integer.parseInt(newText);
                    if (value < min) {
                        change.setText(String.valueOf(min));
                        change.setRange(0, change.getControlText().length());
                    } else if (value > max) {
                        change.setText(String.valueOf(max));
                        change.setRange(0, change.getControlText().length());
                    }
                } else {
                    change.setText(String.valueOf(min));
                    change.setRange(0, change.getControlText().length());
                }
            } catch (NumberFormatException e) {
                return null;
            }
            return change;
        }
        return null;
    }

    protected TextFormatter.Change createNonEmptyStringChange(TextFormatter.Change change) {
        String newText = change.getControlNewText();
        if (!newText.isEmpty() && newText.isBlank()) {
            return null;
        }

        return change;
    }

    @FXML
    protected void chooseDirectoryPath(Event event) {
        Node source = (Node) event.getSource();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(pathChooserLocalizationMap.getOrDefault(source, new SimpleStringProperty("")).getValue());

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            pathChooserPathSettingMap.get(source).setText(selectedDirectory.getAbsolutePath());
        }
    }

    @FXML
    protected void chooseFilePath(Event event) {
        Node source = (Node) event.getSource();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(pathChooserLocalizationMap.getOrDefault(source, new SimpleStringProperty("")).getValue());

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            pathChooserPathSettingMap.get(source).setText(selectedFile.getAbsolutePath());
        }
    }

    @Override
    public void resetToDefaults() {
        resetTextFields();
    }

    protected <T> void addRunConfig(RunConfigurationCreator<T> creator, Project project, T configDescriptor) {
        RunConfig runConfig = creator.createRunConfig(configDescriptor);
        List<RunConfig> runConfigs = new ArrayList<>(runConfigService.getAllRunConfigs(project).getManuallyAddedConfigs());
        runConfigs.add(runConfig);
        runConfigService.saveRunConfigs(project, runConfigs);
    }

    private void resetTextFields() {
        textFields.forEach(textField -> {
            textField.setText("0");
            textField.setText("");
        });
    }
}
