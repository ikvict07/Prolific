package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Setter;
import org.nevertouchgrass.prolific.javafxcontroller.SettingsFooterController;
import org.nevertouchgrass.prolific.javafxcontroller.settings.contract.SettingsOption;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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
    @Setter(onMethod_ = {@Qualifier("settingsStage"), @Autowired})
    protected Stage settingsStage;

    protected Map<Node, StringProperty> directoryChooserLocalizationMap = new HashMap<>();
    protected Map<Node, TextField> directoryChooserPathSettingMap = new HashMap<>();

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

    @FXML
    protected void choosePath(Event event) {
        Node source = (Node) event.getSource();

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(directoryChooserLocalizationMap.getOrDefault(source, new SimpleStringProperty("")).getValue());

        File selectedDirectory = directoryChooser.showDialog(settingsStage);

        if (selectedDirectory != null) {
            directoryChooserPathSettingMap.get(source).setText(selectedDirectory.getAbsolutePath());
        }
    }
}
