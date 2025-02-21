package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.nevertouchgrass.prolific.annotation.AnchorPaneController;
import org.nevertouchgrass.prolific.annotation.Constraints;
import org.nevertouchgrass.prolific.annotation.StageComponent;

@AnchorPaneController
@StageComponent("primaryStage")
public class HeaderController {
    @FXML
    public HBox gradientBox;
    @FXML
    public Circle minimizeButton;
    @FXML
    public Circle maximizeButton;
    @FXML
    public HBox leftSection;
    @FXML
    @Constraints(right = 0.5, left = 0.5)
    public Text titleText;
    @FXML
    @Constraints(right = 0.33)
    public Region rightSection;

    @FXML
    private AnchorPane header;

    @FXML
    private Circle closeButton;

    private double xOffset = 0;
    private double yOffset = 0;
    private Stage stage;
    private double minWidth = 1280;
    private double minHeight = 720;

    public void setStage() {
        closeButton.setOnMouseClicked(this::handleClose);
        minimizeButton.setOnMouseClicked(this::handleMinimize);
        maximizeButton.setOnMouseClicked(this::handleMaximize);

        header.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        header.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.getScene().setOnMouseMoved(this::resizeCursor);
        stage.getScene().setOnMouseDragged(this::resizeWindow);
    }

    private void resizeCursor(MouseEvent event) {
        double border = 8;
        Stage stage = this.stage;
        double x = event.getSceneX();
        double y = event.getSceneY();
        double width = stage.getWidth();
        double height = stage.getHeight();

        if (x < border && y > height - border) {
            stage.getScene().setCursor(javafx.scene.Cursor.SW_RESIZE);
        } else if (x > width - border && y > height - border) {
            stage.getScene().setCursor(javafx.scene.Cursor.SE_RESIZE);
        } else if (x < border) {
            stage.getScene().setCursor(javafx.scene.Cursor.W_RESIZE);
        } else if (x > width - border) {
            stage.getScene().setCursor(javafx.scene.Cursor.E_RESIZE);
        } else if (y > height - border) {
            stage.getScene().setCursor(javafx.scene.Cursor.S_RESIZE);
        } else {
            stage.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    private void resizeWindow(MouseEvent event) {
        double border = 8;
        Stage stage = this.stage;

        double x = event.getSceneX();
        double y = event.getSceneY();
        double screenX = event.getScreenX();

        if (stage.getScene().getCursor() == javafx.scene.Cursor.SW_RESIZE) {
            double offset = screenX - stage.getX();
            double newWidth = stage.getWidth() - offset;
            double newHeight = y;

            if (newWidth > minWidth) {
                stage.setX(screenX);
                stage.setWidth(newWidth);
            }
            if (newHeight > minHeight) {
                stage.setHeight(newHeight);
            }

        } else if (stage.getScene().getCursor() == javafx.scene.Cursor.SE_RESIZE) {
            double newWidth = x;
            double newHeight = y;
            if (newWidth > minWidth)
                stage.setWidth(newWidth);
            if (newHeight > minHeight)
                stage.setHeight(newHeight);
        } else if (stage.getScene().getCursor() == javafx.scene.Cursor.W_RESIZE) {
            double offset = screenX - stage.getX();
            double newWidth = stage.getWidth() - offset;

            if (newWidth > minWidth) {
                stage.setX(screenX);
                stage.setWidth(newWidth);
            }

        } else if (stage.getScene().getCursor() == javafx.scene.Cursor.E_RESIZE) {
            double newWidth = x;
            if (newWidth > minWidth)
                stage.setWidth(newWidth);
        } else if (stage.getScene().getCursor() == javafx.scene.Cursor.S_RESIZE) {
            double newHeight = y;
            if (newHeight > minHeight)
                stage.setHeight(newHeight);
        }
    }

    public void handleClose(MouseEvent mouseEvent) {
        System.out.println("Close");
        if (Platform.isFxApplicationThread()) {
            stage.close();
        } else {
            Platform.runLater(() -> stage.close());
        }
        System.out.println("Closed");

    }

    public void handleMinimize(MouseEvent mouseEvent) {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    public void handleMaximize(MouseEvent mouseEvent) {
        if (stage != null) {
            if (stage.isMaximized()) {
                stage.setIconified(false);
            } else {
                stage.setMaximized(true);
            }
        }
    }
}
