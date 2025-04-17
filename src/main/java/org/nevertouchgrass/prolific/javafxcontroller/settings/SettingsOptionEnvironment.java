package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.components.ArrayListHolder;

import java.util.ArrayList;
import java.util.List;

@StageComponent(stage = "settingsStage")
public class SettingsOptionEnvironment extends AbstractSettingsOption {

    @FXML public ArrayListHolder<Node> options;

    @FXML public Label pythonPath;
    @FXML public Label gradlePath;
    @FXML public Label mvnPath;
    @FXML public Label jdkPath;

    @FXML public StackPane pythonPathChooser;
    @FXML public StackPane gradlePathChooser;
    @FXML public StackPane mvnPathChooser;
    @FXML public StackPane jdkPathChooser;

    @FXML public Label pythonPathErrorMessage;
    @FXML public Label gradlePathErrorMessage;
    @FXML public Label mvnPathErrorMessage;
    @FXML public Label jdkPathErrorMessage;

    @FXML public TextField pythonPathSetting;
    @FXML public TextField gradlePathSetting;
    @FXML public TextField mvnPathSetting;
    @FXML public TextField jdkPathSetting;

    @Initialize
    public void init() {
        fxmlProvider.getFxmlResource("settingsOptionEnvironment");

        directoryChooserLocalizationMap.put(pythonPathChooser, localizationProvider.setting_python_path());
        directoryChooserLocalizationMap.put(gradlePathChooser, localizationProvider.setting_gradle_path());
        directoryChooserLocalizationMap.put(mvnPathChooser, localizationProvider.setting_maven_path());
        directoryChooserLocalizationMap.put(jdkPathChooser, localizationProvider.setting_jdk_path());

        directoryChooserPathSettingMap.put(pythonPathChooser, pythonPathSetting);
        directoryChooserPathSettingMap.put(gradlePathChooser, gradlePathSetting);
        directoryChooserPathSettingMap.put(mvnPathChooser, mvnPathSetting);
        directoryChooserPathSettingMap.put(jdkPathChooser, jdkPathSetting);

        setupValidators();
    }

    @Override
    public void setupValidators() {
        pythonPathSetting.setText(userSettingsHolder.getPythonPath());
        pythonPathSetting.textProperty().addListener((_, _, _) -> pathChangedListener(pythonPathSetting, pythonPathErrorMessage));

        gradlePathSetting.setText(userSettingsHolder.getGradlePath());
        gradlePathSetting.textProperty().addListener((_, _, _) -> pathChangedListener(gradlePathSetting, gradlePathErrorMessage));

        mvnPathSetting.setText(userSettingsHolder.getMavenPath());
        mvnPathSetting.textProperty().addListener((_, _, _) -> pathChangedListener(mvnPathSetting, mvnPathErrorMessage));

        jdkPathSetting.setText(userSettingsHolder.getJdkPath());
        jdkPathSetting.textProperty().addListener((_, _, _) -> pathChangedListener(jdkPathSetting, jdkPathErrorMessage));
    }

    @Override
    public boolean validInput() {
        return checkProvidedPath(pythonPathSetting.getText(), pythonPathSetting, pythonPathErrorMessage) &
                checkProvidedPath(gradlePathSetting.getText(), gradlePathSetting, gradlePathErrorMessage) &
                checkProvidedPath(mvnPathSetting.getText(), mvnPathSetting, mvnPathErrorMessage) &
                checkProvidedPath(jdkPathSetting.getText(), jdkPathSetting, jdkPathErrorMessage);
    }

    @Override
    public boolean checkDefaultValues() {
        boolean value = userSettingsHolder.getPythonPath().toLowerCase().equals(pythonPathSetting.getText()) &&
                userSettingsHolder.getGradlePath().toLowerCase().equals(gradlePathSetting.getText()) &&
                userSettingsHolder.getMavenPath().toLowerCase().equals(mvnPathSetting.getText()) &&
                userSettingsHolder.getJdkPath().toLowerCase().toLowerCase().equals(jdkPathSetting.getText());

        settingsFooterController.changeApplyButtonStyle(value);
        return value;
    }

    @Override
    public boolean saveSettings() {
        if (validInput() && !checkDefaultValues()) {
            userSettingsHolder.setPythonPath(pythonPathSetting.getText());
            userSettingsHolder.setGradlePath(gradlePathSetting.getText());
            userSettingsHolder.setMavenPath(mvnPathSetting.getText());
            userSettingsHolder.setJdkPath(jdkPathSetting.getText());

            userSettingsService.saveSettings();
            return true;
        }
        return false;
    }

    @Override
    public List<Node> getOptions() {
        return new ArrayList<>(options.getItems());
    }

    private void pathChangedListener(TextField path, Label errorMessage) {
        checkDefaultValues();
        path.getStyleClass().remove(ERROR);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }
}
