package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationHolder;
import org.nevertouchgrass.prolific.service.PeriodicalScanningService;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Log4j2
public class SettingsDropdownController {
    @FXML
    private Label settingsLabel;
    @FXML
    private Label scanLabel;
    @FXML
    private Label pluginsLabel;

    private LocalizationHolder localizationHolder;
    private ApplicationEventPublisher applicationEventPublisher;
    private PeriodicalScanningService periodicalScanningService;
    private NotificationService notificationService;

    @FXML
    public void initialize() {
        settingsLabel.textProperty().bind(localizationHolder.getLocalization("settings"));
        scanLabel.textProperty().bind(localizationHolder.getLocalization("scanner"));
        pluginsLabel.textProperty().bind(localizationHolder.getLocalization("plugins"));
    }


    @Autowired
    public void set(PeriodicalScanningService periodicalScanningService, LocalizationHolder localizationHolder, ApplicationEventPublisher applicationEventPublisher, NotificationService notificationService) {
        this.periodicalScanningService = periodicalScanningService;
        this.localizationHolder = localizationHolder;
        this.applicationEventPublisher = applicationEventPublisher;
        this.notificationService = notificationService;
    }

    public void rescan() {
        periodicalScanningService.rescan();
    }

    public void changeLanguage() {
        applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this, Locale.forLanguageTag("sk")));
        log.info("Language changed to Slovak");
        notificationService.notifyInfo(InfoNotification.of("Language changed to Slovak"));
    }
}
