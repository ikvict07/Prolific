package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.PeriodicalScanningService;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.i18n.LocaleContextHolder;
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
    @FXML
    private Label changeLanguageButton;

    private ApplicationEventPublisher applicationEventPublisher;
    private PeriodicalScanningService periodicalScanningService;
    private NotificationService notificationService;

    @Autowired
    public void set(PeriodicalScanningService periodicalScanningService, ApplicationEventPublisher applicationEventPublisher, NotificationService notificationService) {
        this.periodicalScanningService = periodicalScanningService;
        this.applicationEventPublisher = applicationEventPublisher;
        this.notificationService = notificationService;
    }

    public void rescan() {
        periodicalScanningService.rescan();
    }

    public void changeLanguage() {
        Locale locale = LocaleContextHolder.getLocale().equals(Locale.forLanguageTag("sk")) ? Locale.forLanguageTag("en") : Locale.forLanguageTag("sk");
        applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this, locale));
        log.info("Language changed to {}", locale.getDisplayLanguage());
        notificationService.notifyInfo(InfoNotification.of("Language changed to " + locale.getDisplayLanguage(locale)));
    }
}
