package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.configurations.GradleTasksManager;
import org.nevertouchgrass.prolific.service.configurations.creators.GradleTaskRunConfigurationCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    @FXML private StackPane loadingIndicator;

    @Setter(onMethod_ = @Autowired)
    private GradleTasksManager gradleTasksManager;

    @Setter(onMethod_ = @Autowired)
    private GradleTaskRunConfigurationCreator gradleTaskRunConfigurationCreator;

    @Initialize
    public void init() {
        var startTime = System.currentTimeMillis();
        fxmlProvider.getFxmlResource("configsOptionGradle");

        setupValidators();
    }

    @Override
    public void setupValidators() {

        configNameSetting.setText("");
        configNameSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
        configNameSetting.textProperty().addListener((_, _, _) -> textChangeListener(configNameSetting, configNameErrorMessage));

        if (taskSetting.getItems().isEmpty()) {
            taskSetting.setDisable(true);
            taskSetting.getItems().clear();
            Project project = runConfigSettingHeaderController.getProjectPanelController().getProject();

            loadingIndicator.setVisible(true);
            loadingIndicator.setManaged(true);
            CompletableFuture<List<String>> gradleTasks = CompletableFuture.supplyAsync(() -> gradleTasksManager.getGradleTasks(Path.of(project.getPath())));
            gradleTasks.thenAccept(tasks -> {
                Platform.runLater(() -> taskSetting.getItems().addAll(tasks));
                taskSetting.setDisable(false);

                loadingIndicator.setVisible(false);
                loadingIndicator.setManaged(false);
            });
        }


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
            var ccd = new GradleTaskRunConfigurationCreator.GradleTaskDescription();
            ccd.setTitle(configNameSetting.getText().trim());
            ccd.setTaskName(taskSetting.getValue());
            ccd.setOptions(Arrays.stream(argumentsSetting.getText().trim().split("\\s+")).toList());
            RunConfig runConfig = gradleTaskRunConfigurationCreator.createRunConfig(ccd);
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

    @Override
    public void resetToDefaults() {
        configNameSetting.setText("");
        argumentsSetting.setText("");
        taskSetting.getItems().clear();
        taskSetting.setValue(null);
    }
}
