package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.configurations.creators.CustomCommandRunConfigurationCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.List;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class SettingsOptionCommand extends AbstractSettingsOption {
    @FXML private Label configName;
    @FXML private Label command;

    @FXML private Label configNameErrorMessage;
    @FXML private Label commandErrorMessage;
    @FXML private TextField configNameSetting;
    @FXML private TextField commandSetting;

    @Setter(onMethod_ = @Autowired)
    private CustomCommandRunConfigurationCreator creator;

    private boolean isInitialized = false;

    @Initialize
    public void init() {
        fxmlProvider.getFxmlResource("configsOptionCommand");

        textFields.addAll(List.of(configNameSetting, commandSetting));

        setupValidators();
    }

    @Override
    public void setupValidators() {
        configNameSetting.setText("");
        commandSetting.setText("");

        if (!isInitialized) {
            configNameSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
            configNameSetting.textProperty().addListener((_, _, _) -> textChangeListener(configNameSetting, configNameErrorMessage));
            commandSetting.setTextFormatter(new TextFormatter<String>(this::createNonEmptyStringChange));
            commandSetting.textProperty().addListener((_, _, _) -> textChangeListener(commandSetting, commandErrorMessage));
            isInitialized = true;
        }
    }

    @Override
    public boolean validInput() {
        return checkProvidedNonEmptyString(configNameSetting.getText(), configNameSetting, configNameErrorMessage) &
                checkProvidedNonEmptyString(commandSetting.getText(), commandSetting, commandErrorMessage);
    }

    @Override
    public boolean checkDefaultValues() {
        boolean value = commandSetting.getText().isBlank() && configNameSetting.getText().isBlank();

        runConfigFooterController.changeApplyButtonStyle(value);
        return value;
    }

    @Override
    public boolean saveSettings() {
        if (validInput() && !checkDefaultValues()) {
            var ccd = new CustomCommandRunConfigurationCreator.CustomCommandDescription();
            ccd.setTitle(configNameSetting.getText().trim());
            ccd.setCommand(Arrays.stream(commandSetting.getText().trim().split("\\s+")).toList());

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
