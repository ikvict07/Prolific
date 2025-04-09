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
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.EventNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.model.notification.contract.Notification;
import org.nevertouchgrass.prolific.service.notification.contract.NotificationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
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
    @Setter(onMethod_ = @Autowired)
    private Loader loader;
    @Setter(onMethod_ = @Autowired)
    private ContextMenu cancellingPopup;
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
            this.notification.setStyle("-fx-text-fill: red;");
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
}
