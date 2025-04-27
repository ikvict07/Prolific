package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.javafxcontroller.settings.contract.SettingsOption;
import org.nevertouchgrass.prolific.javafxcontroller.settings.options.*;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class RunConfigListController extends AbstractSettingsListController {
    @Setter(onMethod_ = {@Qualifier("configsStage"), @Autowired})
    private Stage configsStage;

    @Setter(onMethod_ = @Autowired)
    private SettingsOptionCommand settingsOptionCommand;
    @Setter(onMethod_ = @Autowired)
    private SettingsOptionGradle settingsOptionGradle;
    @Setter(onMethod_ = @Autowired)
    private SettingsOptionPython settingsOptionPython;
    @Setter(onMethod_ = @Autowired)
    private SettingsOptionAnaconda settingsOptionAnaconda;
    @Setter(onMethod_ = @Autowired)
    private SettingsOptionMaven settingsOptionMaven;

    @Setter(onMethod_ = @Autowired)
    private RunConfigFooterController runConfigFooterController;

    @Setter(onMethod_ = @Autowired)
    private NotificationService notificationService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    @Setter(onMethod_ = @Autowired)
    private RunConfigSettingHeaderController runConfigSettingHeaderController;

    @FXML private Label command;
    @FXML private Label gradle;
    @FXML private Label python;
    @FXML private Label anaconda;
    @FXML private Label maven;

    private final List<SettingsOption> settingsOptions = new ArrayList<>();

    @Initialize
    public void init() {
        settingsOptions.addAll(List.of(settingsOptionCommand, settingsOptionGradle, settingsOptionPython, settingsOptionAnaconda, settingsOptionMaven));

        runConfigFooterController.setSaveRunnable(this::saveSettings);

        configsStage.setOnShowing(_ -> {
            resetToDefaults();
            settingsOptions.forEach(SettingsOption::setupValidators);
            if (currentSettingsOption == null) {
                switchOptions(settingsOptionCommand, command);
            }
        });
    }

    @Override
    public void handleMouseEntered() {
        configsStage.getScene().setCursor(Cursor.DEFAULT);
    }

    @Override
    public void setSettingsList(Event event) {
        Node source = (Node) event.getSource();
        String id = source.getId();
        switch (id) {
            case "command" -> switchOptions(settingsOptionCommand, command);
            case "gradle" -> switchOptions(settingsOptionGradle, gradle);
            case "python" -> switchOptions(settingsOptionPython, python);
            case "anaconda" -> switchOptions(settingsOptionAnaconda, anaconda);
            case "maven" -> switchOptions(settingsOptionMaven, maven);
            default -> switchOptions(settingsOptionCommand, source);
        }
    }

    private void saveSettings() {
        if (currentSettingsOption != null && currentSettingsOption.saveSettings()) {
            runConfigFooterController.changeApplyButtonStyle(true);

            notificationService.notifyInfo(InfoNotification.of(localizationProvider.config_saved()));
            runConfigSettingHeaderController.getProjectPanelController().initializeProjectConfiguration();
        }
    }

    private void resetToDefaults() {
        settingsOptions.forEach(SettingsOption::resetToDefaults);
    }
}
