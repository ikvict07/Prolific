package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.scaners.PeriodicalScanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Log4j2
@SuppressWarnings("unused")
@Lazy
public class SettingsDropdownController {
    @Setter(onMethod_ = {@Qualifier("settingsStage") ,@Autowired})
    private Stage settingsStage;
    @Setter(onMethod_ = {@Qualifier("primaryStage"), @Autowired})
    private Stage primaryStage;
    @FXML
    private Label settingsLabel;
    @FXML
    private Label scanLabel;
    @FXML
    private Label pluginsLabel;
    @FXML
    private Label changeLanguageButton;
    @Setter(onMethod_ = @Autowired)
    private ApplicationEventPublisher applicationEventPublisher;
    @Setter(onMethod_ = @Autowired)
    private PeriodicalScanningService periodicalScanningService;
    @Setter(onMethod_ = @Autowired)
    private NotificationService notificationService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    @Setter(onMethod_ = @Autowired)
    private ApplicationContext applicationContext;


    public void rescan() {
        periodicalScanningService.rescan();
    }

    public void changeLanguage() {
        Locale locale = LocaleContextHolder.getLocale().equals(Locale.forLanguageTag("sk")) ? Locale.forLanguageTag("en") : Locale.forLanguageTag("sk");
        applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this, locale));
        log.info("Language changed to {}", locale.getDisplayLanguage());
        notificationService.notifyInfo(InfoNotification.of(localizationProvider.log_info_language_changed(), locale.getDisplayLanguage(locale)));
    }

    public void openSettings() {
        var settingsScreen = (AnchorPane) applicationContext.getBean("settingsScreenParent");
        settingsStage.setScene(new Scene(settingsScreen));
        settingsStage.initModality(Modality.WINDOW_MODAL);
        settingsStage.initOwner(primaryStage);
        settingsStage.show();
    }
}
