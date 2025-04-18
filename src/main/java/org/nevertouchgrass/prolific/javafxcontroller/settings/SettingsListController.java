package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.javafxcontroller.settings.contract.SettingsOption;
import org.nevertouchgrass.prolific.javafxcontroller.settings.options.SettingsOptionEnvironment;
import org.nevertouchgrass.prolific.javafxcontroller.settings.options.SettingsOptionGeneral;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsListController extends AbstractSettingsListController {
    @Setter(onMethod_ = {@Qualifier("settingsStage"), @Autowired})
    private Stage settingsStage;
    @Setter(onMethod_ = @Autowired)
    private SettingsFooterController settingsFooterController;
    @Setter(onMethod_ = @Autowired)
    private NotificationService notificationService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;

    @Setter(onMethod_ = @Autowired)
    private SettingsOptionGeneral settingsOptionGeneral;
    @Setter(onMethod_ = @Autowired)
    private SettingsOptionEnvironment settingsOptionEnvironment;

    @FXML public Label general;
    @FXML public Label environment;


    private final List<SettingsOption> settingsOptions = new ArrayList<>();

    @FXML
    public void initialize() {
        settingsOptions.addAll(List.of(settingsOptionGeneral, settingsOptionEnvironment));

        settingsFooterController.setSaveRunnable(this::saveSettings);

        settingsStage.setOnShowing(_ -> {
            settingsOptions.forEach(SettingsOption::setupValidators);
            if (currentSettingsOption == null) {
                switchOptions(settingsOptionGeneral, general);
            }
        });
    }

    @Override
    public void handleMouseEntered() {
        settingsStage.getScene().setCursor(Cursor.DEFAULT);
    }

    @Override
    public void setSettingsList(Event event) {
        Node source = (Node) event.getSource();
        String id = source.getId();
        switch (id) {
            case "general" -> switchOptions(settingsOptionGeneral, source);
            case "environment" -> switchOptions(settingsOptionEnvironment, source);
        }
    }

    private void saveSettings() {
        if (currentSettingsOption != null && currentSettingsOption.saveSettings()) {
            settingsFooterController.changeApplyButtonStyle(true);

            notificationService.notifyInfo(InfoNotification.of(localizationProvider.settings_saved()));
        }
    }
}
