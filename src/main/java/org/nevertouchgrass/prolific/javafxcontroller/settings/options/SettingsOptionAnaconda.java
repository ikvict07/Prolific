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
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.configurations.creators.AnacondaRunConfigurationCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class SettingsOptionAnaconda extends AbstractSettingsOption {
    @FXML private Label configName;
    @FXML private Label arguments;
    @FXML private Label scriptPath;
    @FXML private Label anacondaEnv;

    @FXML private Label configNameErrorMessage;
    @FXML private Label scriptPathErrorMessage;
    @FXML private Label anacondaEnvErrorMessage;

    @FXML private TextField configNameSetting;
    @FXML private TextField argumentsSetting;
    @FXML private TextField scriptPathSetting;
    @FXML private TextField anacondaEnvSetting;

    @FXML private StackPane scriptPathChooser;

    @Setter(onMethod_ = @Autowired)
    private AnacondaRunConfigurationCreator creator;

    private boolean isInitialized = false;

    @Initialize
    public void init() {
        fxmlProvider.getFxmlResource("configsOptionAnaconda");

        pathChooserLocalizationMap.put(scriptPathChooser, localizationProvider.setting_script_path());
        pathChooserPathSettingMap.put(scriptPathChooser, scriptPathSetting);

        setupValidators();
    }

    @Override
    public void setupValidators() {
        configNameSetting.setText("");
        argumentsSetting.setText("");
        scriptPathSetting.setText("");
        anacondaEnvSetting.setText("");

        if (!isInitialized) {
            configNameSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
            anacondaEnvSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
            configNameSetting.textProperty().addListener((_, _, _) -> textChangeListener(configNameSetting, configNameErrorMessage));
            scriptPathSetting.textProperty().addListener((_, _, _) -> textChangeListener(scriptPathSetting, scriptPathErrorMessage));
            anacondaEnvSetting.textProperty().addListener((_, _, _) -> textChangeListener(anacondaEnvSetting, anacondaEnvErrorMessage));
            isInitialized = true;
        }
    }

    @Override
    public boolean validInput() {
        return checkProvidedNonEmptyString(configNameSetting.getText(), configNameSetting, configNameErrorMessage) &
                checkProvidedNonEmptyString(anacondaEnvSetting.getText(), anacondaEnvSetting, anacondaEnvErrorMessage) &
                checkProvidedPath(scriptPathSetting.getText(), scriptPathSetting, scriptPathErrorMessage) &&
                checkProvidedNonEmptyString(scriptPathSetting.getText(), scriptPathSetting, scriptPathErrorMessage);
    }

    @Override
    public boolean checkDefaultValues() {
        boolean value = scriptPathSetting.getText().isBlank() && configNameSetting.getText().isBlank() &&
                argumentsSetting.getText().isBlank() && anacondaEnvSetting.getText().isBlank();

        runConfigFooterController.changeApplyButtonStyle(value);
        return value;
    }

    @Override
    public boolean saveSettings() {
        if (validInput() && !checkDefaultValues()) {
            var ccd = new AnacondaRunConfigurationCreator.AnacondaDescription();
            ccd.setTitle(configNameSetting.getText().trim());
            ccd.setScriptPath(scriptPathSetting.getText().trim());
            ccd.setEnvironmentName(anacondaEnvSetting.getText().trim());
            ccd.setScriptArgs(Arrays.stream(scriptPathSetting.getText().trim().split("\\s+")).toList());
            RunConfig runConfig = creator.createRunConfig(ccd);
            Project project = runConfigSettingHeaderController.getProjectPanelController().getProject();
            List<RunConfig> runConfigs = new ArrayList<>(runConfigService.getAllRunConfigs(project).getManuallyAddedConfigs());
            runConfigs.add(runConfig);
            runConfigService.saveRunConfigs(project, runConfigs);

            return true;
        }

        return false;
    }

    @Override
    public void resetToDefaults() {
        configNameSetting.setText("0");
        configNameSetting.setText("");
        scriptPathSetting.setText("0");
        scriptPathSetting.setText("");
        anacondaEnvSetting.setText("0");
        anacondaEnvSetting.setText("");

        argumentsSetting.setText("");
    }

    private void textChangeListener(TextField textField, Label errorMessage) {
        checkDefaultValues();
        textField.getStyleClass().remove(ERROR);
        errorMessage.setVisible(false);
        errorMessage.setManaged(false);
    }
}
