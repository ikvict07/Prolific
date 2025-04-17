package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.HashSet;

public abstract class AbstractHeaderController {
    protected Stage stage;
    private Pane header;

    private double xOffset = 0;
    private double yOffset = 0;

    private double heightBeforeMaximizing;
    private double widthBeforeMaximizing;

    private double xBeforeMaximizing;
    private double yBeforeMaximizing;

    protected final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

    @Setter
    private double minWidth = 0;
    @Setter
    private double minHeight = 0;

    private double endX = 0;

    protected final HashSet<Object> draggablePanes = new HashSet<>();

    protected void setupDragging() {
        header.setOnMousePressed(event -> {
            if (draggablePanes.contains(event.getTarget())) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        header.setOnMouseDragged(event -> {
            if (draggablePanes.contains(event.getTarget())) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
                endX = stage.getX() + stage.getWidth();
            }
        });
    }

    protected void setHeader(Pane header) {
        this.header = header;
    }

    protected void setupResizing() {
        stage.getScene().setOnMouseMoved(this::resizeCursor);
        stage.getScene().setOnMouseDragged(this::resizeWindow);

        stage.setOnShown(_ -> {
            endX = stage.getX() + stage.getWidth();
            widthBeforeMaximizing = stage.getWidth();
            heightBeforeMaximizing = stage.getHeight();
        });

        stage.maximizedProperty().addListener((_, _, newValue) -> {
            if (newValue) {
                stage.getScene().getRoot().setStyle("-fx-background-radius: 0;");
                header.setStyle("-fx-background-radius: 0;");
            } else {
                stage.getScene().getRoot().setStyle("-fx-background-radius: 16;");
                header.setStyle("-fx-background-radius: 16 16 0 0;");
            }
        });
    }

    protected void resizeCursor(MouseEvent event) {
        double border = 8;
        double x = event.getSceneX();
        double y = event.getSceneY();
        double width = stage.getWidth();
        double height = stage.getHeight();

        if (stage.isMaximized()) {
            return;
        }

        if (x < border && y > height - border) {
            stage.getScene().setCursor(Cursor.SW_RESIZE);
        } else if (x > width - border && y > height - border) {
            stage.getScene().setCursor(Cursor.SE_RESIZE);
        } else if (x < border && y > header.getHeight()) {
            stage.getScene().setCursor(Cursor.W_RESIZE);
        } else if (x > width - border && y > header.getHeight()) {
            stage.getScene().setCursor(Cursor.E_RESIZE);
        } else if (y > height - border) {
            stage.getScene().setCursor(Cursor.S_RESIZE);
        } else {
            stage.getScene().setCursor(Cursor.DEFAULT);
        }
    }

    protected void resizeWindow(MouseEvent event) {
        double deltaX = event.getScreenX();
        double deltaY = event.getScreenY();
        switch (org.nevertouchgrass.prolific.constants.Cursor.getCursor(stage.getScene().getCursor())) {
            case SW_RESIZE -> {
                resizeWidth(endX - deltaX, deltaX, true);
                double newHeight = deltaY - stage.getY();
                resizeHeight(newHeight);
            }
            case SE_RESIZE -> {
                resizeWidth(deltaX - stage.getX(), deltaX, false);
                double newHeight = deltaY - stage.getY();
                resizeHeight(newHeight);
            }
            case W_RESIZE -> resizeWidth(endX - deltaX, deltaX, true);
            case E_RESIZE -> resizeWidth(deltaX - stage.getX(), deltaX, false);
            case S_RESIZE -> {
                double newHeight = deltaY - stage.getY();
                resizeHeight(newHeight);
            }

            case null -> throw new IllegalStateException("Unexpected value: " + null);
            case N_RESIZE, NE_RESIZE, NW_RESIZE, DEFAULT -> {
                // No action
            }
        }
    }

    private void resizeWidth(double newWidth, double deltaX, boolean adjustX) {
        widthBeforeMaximizing = newWidth;

        if (newWidth >= minWidth && deltaX >= visualBounds.getMinX() && deltaX <= visualBounds.getMaxX()) {
            if (adjustX) {
                stage.setX(deltaX);
            }
            stage.setWidth(newWidth);
            stage.setHeight(stage.getHeight()); // GTK bug workaround
        }
    }

    private void resizeHeight(double newHeight) {
        heightBeforeMaximizing = newHeight;

        if (newHeight >= minHeight && stage.getY() + newHeight <= visualBounds.getMaxY()) {
            stage.setHeight(newHeight);
            stage.setWidth(stage.getWidth()); // GTK bug workaround
        }
    }

    public void handleMaximize() {
        if (stage.isMaximized()) {
            stage.setMaximized(false);
            stage.setWidth(widthBeforeMaximizing);
            stage.setHeight(heightBeforeMaximizing);
            stage.setX(xBeforeMaximizing);
            stage.setY(yBeforeMaximizing);
            endX = stage.getX() + stage.getWidth();
        } else {
            xBeforeMaximizing = stage.getX();
            yBeforeMaximizing = stage.getY();
            widthBeforeMaximizing = stage.getWidth();
            heightBeforeMaximizing = stage.getHeight();
            stage.setMaximized(true);
        }
    }

    public void handleMinimize() {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    public void handleClose() {
        if (Platform.isFxApplicationThread()) {
            stage.close();
        } else {
            Platform.runLater(() -> stage.close());
        }
    }

    public void handleHeaderMaximize(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && draggablePanes.contains(mouseEvent.getTarget())) {
            handleMaximize();
        }
    }
}
