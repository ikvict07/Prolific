package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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

    @Autowired
    private void setLoader(Loader loader) {
        this.loader = loader;
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
                loaderPane.getChildren().clear();
                loaderPane.getChildren().add(loader.createLoader());
            });
            case END_PROJECT_SCAN -> Platform.runLater(() -> loaderPane.getChildren().clear());
        }
    }
}
