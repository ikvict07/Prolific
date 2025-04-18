package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.configurations.GradleTasksManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class SettingsOptionGradle extends AbstractSettingsOption {
    @FXML private Label configName;
    @FXML private Label arguments;
    @FXML private Label task;

    @FXML private Label configNameErrorMessage;
    @FXML private Label taskErrorMessage;

    @FXML private TextField configNameSetting;
    @FXML private TextField argumentsSetting;
    @FXML private ComboBox<String> taskSetting;

    @Setter(onMethod_ = @Autowired)
    private GradleTasksManager gradleTasksManager;

    @Initialize
    public void init() {
        fxmlProvider.getFxmlResource("configsOptionGradle");

        setupValidators();
    }

    @Override
    public void setupValidators() {
        configNameSetting.setText("");
        configNameSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
        configNameSetting.textProperty().addListener((_, _, _) -> textChangeListener(configNameSetting, configNameErrorMessage));

        Project project = runConfigSettingHeaderController.getProjectPanelController().getProject();
        taskSetting.getItems().clear();
        taskSetting.getItems().addAll(gradleTasksManager.getGradleTasks(Path.of(project.getPath())));

        argumentsSetting.setText("");
        argumentsSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
    }

    @Override
    public boolean validInput() {
        return checkProvidedNonEmptyString(configNameSetting.getText(), configNameSetting, configNameErrorMessage) &
                checkNonEmptyTask(taskSetting, taskErrorMessage);
    }

    @Override
    public boolean checkDefaultValues() {
        boolean value = taskSetting.getValue() == null && configNameSetting.getText().isBlank();

        runConfigFooterController.changeApplyButtonStyle(value);
        return value;
    }

    @Override
    public boolean saveSettings() {
        if (validInput() && !checkDefaultValues()) {
            List<String> command = new ArrayList<>();
            command.add("./gradlew");
            command.add(taskSetting.getValue());
            command.addAll(Arrays.stream(argumentsSetting.getText().trim().split("\\s+")).toList());
            RunConfig runConfig = new RunConfig();
            runConfig.setCommand(command);
            runConfig.setConfigName(configNameSetting.getText().trim());
            runConfig.setType("Gradle");

            Project project = runConfigSettingHeaderController.getProjectPanelController().getProject();
            List<RunConfig> runConfigs = new ArrayList<>(runConfigService.getAllRunConfigs(project).getManuallyAddedConfigs());
            runConfigs.add(runConfig);
            runConfigService.saveRunConfigs(project, runConfigs);

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

    private boolean checkNonEmptyTask(ComboBox<String> taskSetting, Label taskErrorMessage) {
        if (taskSetting.getValue() == null) {
            if (!taskSetting.getStyleClass().contains(ERROR)) {
                taskSetting.getStyleClass().add(ERROR);
            }
            taskErrorMessage.setVisible(true);
            taskErrorMessage.setManaged(true);
            return false;
        }
        taskSetting.getStyleClass().remove(ERROR);
        taskErrorMessage.setVisible(false);
        taskErrorMessage.setManaged(false);

        return true;
    }
}
