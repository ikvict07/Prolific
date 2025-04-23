package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.configurations.creators.PythonRunConfigurationCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class SettingsOptionPython extends AbstractSettingsOption {
    @FXML private Label configName;
    @FXML private Label arguments;
    @FXML private Label scriptPath;

    @FXML private Label configNameErrorMessage;
    @FXML private Label scriptPathErrorMessage;

    @FXML private TextField configNameSetting;
    @FXML private TextField argumentsSetting;
    @FXML private TextField scriptPathSetting;

    @FXML private StackPane scriptPathChooser;

    @Setter(onMethod_ = @Autowired)
    private PythonRunConfigurationCreator creator;

    private boolean isInitialized = false;

    @Initialize
    public void init() {
        fxmlProvider.getFxmlResource("configsOptionPython");

        textFields.addAll(List.of(configNameSetting, argumentsSetting, scriptPathSetting));

        pathChooserLocalizationMap.put(scriptPathChooser, localizationProvider.setting_script_path());
        pathChooserPathSettingMap.put(scriptPathChooser, scriptPathSetting);

        setupValidators();
    }

    @Override
    public void setupValidators() {
        configNameSetting.setText("");
        argumentsSetting.setText("");
        scriptPathSetting.setText("");

        if (!isInitialized) {
            configNameSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
            configNameSetting.textProperty().addListener((_, _, _) -> textChangeListener(configNameSetting, configNameErrorMessage));
            scriptPathSetting.textProperty().addListener((_, _, _) -> textChangeListener(scriptPathSetting, scriptPathErrorMessage));
            isInitialized = true;
        }
    }

    @Override
    public boolean validInput() {
        var project = runConfigSettingHeaderController.getProjectPanelController().getProject();
        var path = Path.of(project.getPath()).resolve(scriptPathSetting.getText());
        return checkProvidedNonEmptyString(configNameSetting.getText(), configNameSetting, configNameErrorMessage) &
                checkProvidedPath(path.toString(), scriptPathSetting, scriptPathErrorMessage) &&
                checkProvidedNonEmptyString(scriptPathSetting.getText(), scriptPathSetting, scriptPathErrorMessage);
    }

    @Override
    public boolean checkDefaultValues() {
        boolean value = scriptPathSetting.getText().isBlank() && configNameSetting.getText().isBlank() && argumentsSetting.getText().isBlank();

        runConfigFooterController.changeApplyButtonStyle(value);
        return value;
    }

    @Override
    public boolean saveSettings() {
        if (validInput() && !checkDefaultValues()) {
            var ccd = new PythonRunConfigurationCreator.PythonConfigDescription();
            ccd.setTitle(configNameSetting.getText().trim());
            ccd.setScriptPath(scriptPathSetting.getText().trim());
            ccd.setArguments(Arrays.stream(argumentsSetting.getText().trim().split("\\s+")).toList());

            Project project = runConfigSettingHeaderController.getProjectPanelController().getProject();
            addRunConfig(creator, project, ccd);

            return true;
        }

        return false;
    }

    private void textChangeListener(TextField textField, Label errorMessage) {
        checkDefaultValues();
        textField.getStyleClass().remove(ERROR);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }
}
