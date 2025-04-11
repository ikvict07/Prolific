package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;


@Component
@SuppressWarnings("unused")
public class SettingsHeaderController {
    @FXML
    private AnchorPane header;
    public Circle closeButton;
    public Circle minimizeButton;
    public Circle maximizeButton;
    public Label titleText;
    public HBox gradientBox;

    private Stage stage;

    private final double xOffset = 0;
    private final double yOffset = 0;

    private double heightBeforeMaximizing;
    private double widthBeforeMaximizing;

    private double xBeforeMaximizing;
    private double yBeforeMaximizing;

    private final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

    private final double minWidth = visualBounds.getMaxX() / 1.5;
    private final double minHeight = visualBounds.getMaxY() / 1.5;

    @FXML public void initialize() {
        stage = new Stage();
    }


    public void handleHeaderMaximize() {
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            stage.setWidth(widthBeforeMaximizing);
            stage.setHeight(heightBeforeMaximizing);
            stage.setX(xBeforeMaximizing);
            stage.setY(yBeforeMaximizing);
            double endX = stage.getX() + stage.getWidth();
        } else {
            xBeforeMaximizing = stage.getX();
            yBeforeMaximizing = stage.getY();
            widthBeforeMaximizing = stage.getWidth();
            heightBeforeMaximizing = stage.getHeight();
            stage.setMaximized(true);
        }

    }

    public void handleClose() {
    }

    public void handleMinimize() {
    }

    public void handleMaximize() {
    }
}
