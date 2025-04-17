package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.javafxcontroller.settings.SettingsOptionEnvironment;
import org.nevertouchgrass.prolific.javafxcontroller.settings.SettingsOptionGeneral;
import org.nevertouchgrass.prolific.javafxcontroller.settings.contract.SettingsOption;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

@StageComponent(stage = "settingsStage")
public class SettingsListController {
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
    @FXML public GridPane settingsList;
    @FXML public Label environment;

    private static final String SELECTED = "selected";

    private final List<SettingsOption> settingsOptions = new ArrayList<>();
    private final List<Node> settingsLabels = new ArrayList<>();
    private SettingsOption currentSettingsOption;

    @FXML
    public void initialize() {
        settingsOptions.addAll(List.of(settingsOptionGeneral, settingsOptionEnvironment));
        settingsLabels.addAll(List.of(general, environment));


        settingsFooterController.setSaveRunnable(this::saveSettings);

        settingsStage.setOnShowing(_ -> {
            settingsOptions.forEach(SettingsOption::setupValidators);
            if (currentSettingsOption == null) {
                switchOptions(settingsOptionGeneral, general);
            }
        });
    }

    public void handleMouseEntered() {
        settingsStage.getScene().setCursor(Cursor.DEFAULT);
    }

    private void saveSettings() {
        if (currentSettingsOption != null && currentSettingsOption.saveSettings()) {
            settingsFooterController.changeApplyButtonStyle(true);

            notificationService.notifyInfo(InfoNotification.of(localizationProvider.settings_saved()));
        }
    }

    @FXML
    private void setSettingsList(Event event) {
        Node source = (Node) event.getSource();
        String id = source.getId();
        switch (id) {
            case "general" -> switchOptions(settingsOptionGeneral, source);
            case "environment" -> switchOptions(settingsOptionEnvironment, source);
        }
    }

    private void switchOptions(SettingsOption option, Node label) {
        if (currentSettingsOption == option) {
            return;
        }

        if (currentSettingsOption != null) {
            currentSettingsOption.setupValidators();
        }

        settingsLabels.forEach(l -> l.getStyleClass().remove(SELECTED));
        label.getStyleClass().add(SELECTED);
        currentSettingsOption = option;
        settingsList.getChildren().clear();
        settingsList.getChildren().addAll(option.getOptions());
        option.setupValidators();
    }
}
