package org.nevertouchgrass.prolific.javafxcontroller;


import jakarta.annotation.PostConstruct;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "configsStage")
@Slf4j
@Lazy
public class RunConfigSettingHeaderController extends AbstractHeaderController {
    @FXML
    public AnchorPane configsHeader;
    @FXML
    public Label titleText;
    @FXML
    public HBox configsGradientBox;
    @FXML
    public Circle closeButton;
    @FXML
    public Circle maximizeButton;
    @FXML
    public Circle minimizeButton;
    private boolean isStageInitialized = false;
    @Setter(onMethod_ = @Autowired)
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    public void setStage(@Qualifier("configsStage") Stage stage) {
        this.stage = stage;
    }
    @Setter(onMethod_ = {@Lazy, @Autowired})
    private ObjectProvider<Parent> configsScreenParent;
    @Setter(onMethod_ = {@Qualifier("primaryStage"), @Autowired})
    private Stage primaryStage;

    @PostConstruct
    public void setupStage() {
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);
    }
    @Initialize
    public void init() {
        setMinWidth(minWidth);
        setMinHeight(minHeight);

        setHeader(configsHeader);
        setupDragging();
        setupResizing();

        draggablePanes.add(configsHeader);
        draggablePanes.add(configsGradientBox);
        draggablePanes.add(titleText);
    }

    public void open() {
        if (!isStageInitialized) {
            isStageInitialized = true;
            setupScene();
            eventPublisher.publishEvent(new StageInitializeEvent("configsStage"));
        }
        stage.show();
    }
    double minWidth = visualBounds.getMaxX() / 2;
    double minHeight = visualBounds.getMaxY() / 2;

    private void setupScene() {
        Scene scene = new Scene(configsScreenParent.getIfAvailable(), minWidth, minHeight);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
    }
}
