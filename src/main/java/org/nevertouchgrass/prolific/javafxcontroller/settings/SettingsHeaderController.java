package org.nevertouchgrass.prolific.javafxcontroller.settings;

import jakarta.annotation.PostConstruct;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.nevertouchgrass.prolific.javafxcontroller.AbstractHeaderController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "settingsStage")
@Slf4j
@SuppressWarnings("unused")
@Lazy
public class SettingsHeaderController extends AbstractHeaderController {
    @FXML
    private AnchorPane settingsHeader;
    @FXML
    public Node closeButton;
    @FXML
    public Node minimizeButton;
    @FXML
    public Node maximizeButton;
    @FXML
    public Label titleText;
    @FXML
    public HBox settingsGradientBox;
    private boolean isStageInitialized = false;

    @Autowired
    public void setStage(@Qualifier("settingsStage") Stage stage) {
        this.stage = stage;
    }
    @Setter(onMethod_ = @Autowired)
    private ApplicationEventPublisher eventPublisher;
    @Setter(onMethod_ = {@Qualifier("primaryStage"), @Autowired})
    private Stage primaryStage;

    @Setter(onMethod_ = {@Lazy, @Autowired})
    private ObjectProvider<Parent> settingsScreenParent;


    @PostConstruct
    public void setupStage() {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
    }

    double minWidth = visualBounds.getMaxX() / 2;
    double minHeight = visualBounds.getMaxY() / 2;

    @Initialize
    public void init() {
        setMinWidth(minWidth);
        setMinHeight(minHeight);

        setHeader(settingsHeader);
        setupDragging();
        setupResizing();

        draggablePanes.add(settingsHeader);
        draggablePanes.add(settingsGradientBox);
        draggablePanes.add(titleText);

        setupMaximizeButton(maximizeButton);
    }

    @Override
    public void handleClose() {
        log.info("Closing settings");
        super.handleClose();
        log.info("Settings closed");
    }

    public void open() {
        if (!isStageInitialized) {
            isStageInitialized = true;
            setupScene();
            eventPublisher.publishEvent(new StageInitializeEvent("settingsStage"));
        }
        stage.show();
    }

    private void setupScene() {
        Scene scene = new Scene(settingsScreenParent.getIfAvailable(), minWidth, minHeight);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
    }
}
