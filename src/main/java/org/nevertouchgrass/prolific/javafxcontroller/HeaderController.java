package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.event.EventTarget;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.nevertouchgrass.prolific.annotation.AnchorPaneController;
import org.nevertouchgrass.prolific.annotation.Constraints;
import org.nevertouchgrass.prolific.annotation.ConstraintsIgnoreElementSize;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.util.Set;

@AnchorPaneController
@StageComponent("primaryStage")
@SuppressWarnings("unused")
public class HeaderController {

    private ApplicationContext applicationContext;
    @FXML
    public StackPane settingsButton;
    @FXML
    public Circle minimizeButton;
    @FXML
    public Circle maximizeButton;
    @FXML
    @ConstraintsIgnoreElementSize(right = 0.66)
    public HBox gradientBox;
    @FXML
    @Constraints(right = 0.5, left = 0.5)
    public Text titleText;
    @FXML
    @ConstraintsIgnoreElementSize(right = 0.33)
    public Region rightSection;

    @FXML
    private AnchorPane header;

    @FXML
    private Circle closeButton;

    @Autowired
    public void setSettingsPopup(Popup settingsPopup) {
        this.settingsPopup = settingsPopup;
    }

    private Popup settingsPopup;

    private double xOffset = 0;
    private double yOffset = 0;
    private Stage stage;

    private double heightBeforeMaximizing;
    private double widthBeforeMaximizing;

    private double xBeforeMaximizing;
    private double yBeforeMaximizing;

    private final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

    private final double minWidth = visualBounds.getMaxX() / 1.5;
    private final double minHeight = visualBounds.getMaxY() / 1.5;

    private double endX = 0;

    private Set<Object> headerMaximizeAndDragComponents;

    @Initialize
    public void init() {
        closeButton.setOnMouseClicked(this::handleClose);
        minimizeButton.setOnMouseClicked(this::handleMinimize);
        maximizeButton.setOnMouseClicked(this::handleMaximize);

        header.setOnMousePressed(event -> {
            if (isHeaderMaximizeAndDragComponent(event.getTarget())) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });

        header.setOnMouseDragged(event -> {
            if (isHeaderMaximizeAndDragComponent(event.getTarget())) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
                endX = stage.getX() + stage.getWidth();
            }
        });

        stage.getScene().setOnMouseMoved(this::resizeCursor);
        stage.getScene().setOnMouseDragged(this::resizeWindow);

        stage.setOnShown(_ -> {
            endX = stage.getX() + stage.getWidth();
            widthBeforeMaximizing = stage.getWidth();
            heightBeforeMaximizing = stage.getHeight();
        });

        headerMaximizeAndDragComponents = Set.of(gradientBox, titleText, header);
    }

    private void resizeCursor(MouseEvent event) {
        double border = 8;
        double x = event.getSceneX();
        double y = event.getSceneY();
        double width = stage.getWidth();
        double height = stage.getHeight();

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

    private void resizeWindow(MouseEvent event) {
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
        if (newWidth >= minWidth && deltaX >= visualBounds.getMinX() && deltaX <= visualBounds.getMaxX()) {
            if (adjustX) {
                stage.setX(deltaX);
            }
            widthBeforeMaximizing = newWidth;
            stage.setWidth(newWidth);
            stage.setHeight(stage.getHeight());
        }
    }

    private void resizeHeight(double newHeight) {
        if (newHeight >= minHeight && stage.getY() + newHeight <= visualBounds.getMaxY()) {
            heightBeforeMaximizing = newHeight;
            stage.setHeight(newHeight);
            stage.setWidth(stage.getWidth());
        }
    }

    public void handleClose(MouseEvent mouseEvent) {
        if (Platform.isFxApplicationThread()) {
            stage.close();
        } else {
            Platform.runLater(() -> stage.close());
        }
        SpringApplication.exit(applicationContext);
    }

    public void handleMinimize(MouseEvent mouseEvent) {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    public void handleMaximize(MouseEvent mouseEvent) {
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

    public void handleHeaderMaximize(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2 && isHeaderMaximizeAndDragComponent(mouseEvent.getTarget())) {
            handleMaximize(mouseEvent);
        }
    }

    public void dropdownForSettings() {
        Bounds bounds = settingsButton.localToScreen(settingsButton.getBoundsInLocal());
        settingsPopup.setX(bounds.getMinX());
        settingsPopup.setY(bounds.getMaxY());
        settingsPopup.show(stage);
    }

    public void projects(MouseEvent mouseEvent) {
        // TODO: Implement
    }

    @Autowired
    public void set(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private boolean isHeaderMaximizeAndDragComponent(EventTarget eventTarget) {
        return headerMaximizeAndDragComponents.contains(eventTarget);
    }
}
