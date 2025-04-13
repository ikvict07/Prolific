package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.EventNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.model.notification.contract.Notification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.notification.contract.NotificationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Objects;

@Lazy
@StageComponent
@Log4j2
public class FooterController implements NotificationListener<Notification> {
    @FXML
    public AnchorPane footer;
    @FXML
    public HBox content;
    @FXML
    public StackPane loaderPane;
    @FXML
    public StackPane logPane;
    @FXML
    public Label notification;
    @FXML
    public StackPane localePane;
    @FXML
    public Label localeLabel;
    @Setter(onMethod_ = @Autowired)
    private Loader loader;
    @Setter(onMethod_ = @Autowired)
    private ContextMenu cancellingPopup;
    @Setter(onMethod_ = @Autowired)
    private ApplicationEventPublisher applicationEventPublisher;
    @Setter(onMethod_ = @Autowired)
    private NotificationService notificationService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    @Override
    public void onNotification(Notification notification) {
        if (notification instanceof InfoNotification in) {
            onInfoNotification(in);
        }
        if (notification instanceof EventNotification ev) {
            onEventNotification(ev);
        }
        if (notification instanceof ErrorNotification en) {
            onErrorNotification(en);
        }
    }

    private void onInfoNotification(InfoNotification notification) {
        Platform.runLater(() -> {
            this.notification.setStyle("-fx-text-fill: #DFE1E5;");
            this.notification.textProperty().unbind();
            this.notification.textProperty().bind(notification.getPayload());
        });
    }

    private void onErrorNotification(ErrorNotification notification) {
        Platform.runLater(() -> {
            this.notification.setStyle("-fx-text-fill: #DB5C5C;");
            this.notification.textProperty().unbind();
            this.notification.textProperty().bind(notification.getPayload().message());
        });
    }

    private void onEventNotification(EventNotification notification) {
        var event = notification.getPayload();
        if (Objects.requireNonNull(event) == EventNotification.EventType.START_PROJECT_SCAN) {
            Platform.runLater(() -> {
                loaderPane.setVisible(true);
                loaderPane.getChildren().clear();
                loaderPane.getChildren().add(loader.createLoader());
            });
        } else if (event == EventNotification.EventType.END_PROJECT_SCAN) {
            Platform.runLater(() -> {
                loaderPane.setVisible(false);
                loaderPane.getChildren().clear();
            });
        }
    }

    public void showCancelPopup() {
        Bounds contentBounds = content.localToScreen(content.getBoundsInLocal());
        Bounds footerBounds = footer.localToScreen(footer.getBoundsInLocal());
        Stage footerStage = (Stage) footer.getScene().getWindow();

        cancellingPopup.setX(contentBounds.getMinX());
        cancellingPopup.setY(contentBounds.getMinY());
        cancellingPopup.show(footerStage);

        Platform.runLater(() -> {
            double popupWidth = cancellingPopup.getWidth();
            double popupHeight = cancellingPopup.getHeight();

            double rightAlignedX = footerStage.getX() + footerStage.getWidth() - popupWidth + 8;
            double topAlignedY = footerBounds.getMinY() - popupHeight / 4 * 3;
            cancellingPopup.setX(rightAlignedX);
            cancellingPopup.setY(topAlignedY);
        });
    }

    public void changeLanguage() {
        Locale locale = LocaleContextHolder.getLocale().equals(Locale.forLanguageTag("sk")) ? Locale.forLanguageTag("en") : Locale.forLanguageTag("sk");
        applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this, locale));
        log.info("Language changed to {}", locale.getDisplayLanguage());
        notificationService.notifyInfo(InfoNotification.of(localizationProvider.log_info_language_changed(), locale.getDisplayLanguage(locale)));
    }

}
