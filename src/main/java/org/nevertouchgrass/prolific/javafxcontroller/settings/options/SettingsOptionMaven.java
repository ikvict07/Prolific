package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.configurations.creators.MavenRunConfigurationCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class SettingsOptionMaven extends AbstractSettingsOption {
    @FXML private Label configName;
    @FXML private Label arguments;
    @FXML private Label task;

    @FXML private Label configNameErrorMessage;
    @FXML private Label taskErrorMessage;

    @FXML private TextField configNameSetting;
    @FXML private TextField argumentsSetting;
    @FXML private TextField taskSetting;


    @Setter(onMethod_ = @Autowired)
    private MavenRunConfigurationCreator creator;

    private boolean isInitialized = false;

    @Initialize
    public void init() {
        fxmlProvider.getFxmlResource("configsOptionMaven");

        textFields.addAll(List.of(configNameSetting, argumentsSetting, taskSetting));

        setupValidators();
    }

    @Override
    public void setupValidators() {
        configNameSetting.setText("");
        argumentsSetting.setText("");
        taskSetting.setText("");

        if (!isInitialized) {
            configNameSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
            taskSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
            configNameSetting.textProperty().addListener((_, _, _) -> textChangeListener(configNameSetting, configNameErrorMessage));
            taskSetting.textProperty().addListener((_, _, _) -> textChangeListener(taskSetting, taskErrorMessage));
            isInitialized = true;
        }
    }

    @Override
    public boolean validInput() {
        return checkProvidedNonEmptyString(configNameSetting.getText(), configNameSetting, configNameErrorMessage) &
                checkProvidedNonEmptyString(taskSetting.getText(), taskSetting, taskErrorMessage);
    }

    @Override
    public boolean checkDefaultValues() {
        boolean value = configNameSetting.getText().isBlank() && argumentsSetting.getText().isBlank() &&
                taskSetting.getText().isBlank();

        runConfigFooterController.changeApplyButtonStyle(value);
        return value;
    }

    @Override
    public boolean saveSettings() {
        if (validInput() && !checkDefaultValues()) {
            var ccd = new MavenRunConfigurationCreator.MavenDescription();
            ccd.setTitle(configNameSetting.getText().trim());
            ccd.setOptions(Arrays.stream(argumentsSetting.getText().trim().split("\\s+")).toList());
            ccd.setGoal(taskSetting.getText().trim());

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
