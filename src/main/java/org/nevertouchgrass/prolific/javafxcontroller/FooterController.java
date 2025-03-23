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
import javafx.util.Pair;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.EventNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.model.notification.Notification;
import org.nevertouchgrass.prolific.service.notification.contract.NotificationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@StageComponent("primaryStage")
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
    private Stage stage;

    private Loader loader;

    private ContextMenu cancellingPopup;


    @Autowired
    private void set(Loader loader, Pair<CancellingDropdownController, ContextMenu> cancelPopup) {
        this.loader = loader;
        cancellingPopup = cancelPopup.getValue();
    }

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
            this.notification.setText(notification.getPayload());
        });
    }

    private void onErrorNotification(ErrorNotification notification) {
        Platform.runLater(() -> {
            this.notification.setText(notification.getPayload().message());
            this.notification.setStyle("-fx-text-fill: red;");
        });
    }

    private void onEventNotification(EventNotification notification) {
        var event = notification.getPayload();
        switch (event) {
            case START_PROJECT_SCAN -> Platform.runLater(() -> {
                loaderPane.setVisible(true);
                loaderPane.getChildren().clear();
                loaderPane.getChildren().add(loader.createLoader());
            });
            case END_PROJECT_SCAN -> Platform.runLater(() -> {
                loaderPane.setVisible(false);
                loaderPane.getChildren().clear();
            });
        }
    }

    public void showCancelPopup() {
        Bounds contentBounds = content.localToScreen(content.getBoundsInLocal());
        Bounds footerBounds = footer.localToScreen(footer.getBoundsInLocal());
        Stage stage = (Stage) footer.getScene().getWindow();

        cancellingPopup.setX(contentBounds.getMinX());
        cancellingPopup.setY(contentBounds.getMinY());
        cancellingPopup.show(stage);

        Platform.runLater(() -> {
            double popupWidth = cancellingPopup.getWidth();
            double popupHeight = cancellingPopup.getHeight();

            double rightAlignedX = stage.getX() + stage.getWidth() - popupWidth + 8;
            double topAlignedY = footerBounds.getMinY() - popupHeight / 4 * 3;
            cancellingPopup.setX(rightAlignedX);
            cancellingPopup.setY(topAlignedY);
        });
    }
}
