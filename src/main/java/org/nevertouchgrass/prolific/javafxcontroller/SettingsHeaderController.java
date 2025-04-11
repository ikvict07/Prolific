package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@SuppressWarnings("unused")
@Lazy
public class SettingsHeaderController extends AbstractHeaderController {
    @FXML
    public AnchorPane settingsHeader;
    @FXML
    private AnchorPane header;
    @FXML
    public Circle closeButton;
    @FXML
    public Circle minimizeButton;
    @FXML
    public Circle maximizeButton;
    @FXML
    public Label titleText;
    @FXML
    public HBox settingsGradientBox;

    @Autowired
    public void setStage(@Qualifier("settingsStage") Stage stage) {
        this.stage = stage;
    }

    @Setter(onMethod_ = {@Qualifier("primaryStage"), @Autowired})
    private Stage primaryStage;

    @Setter(onMethod_ = @Autowired)
    private ApplicationContext applicationContext;

    @Initialize
    public void init() {
        var settingsScreen = (AnchorPane) applicationContext.getBean("settingsScreenParent");

        double minWidth = visualBounds.getMaxX() / 2;
        double minHeight = visualBounds.getMaxY() / 2;
        setMinWidth(minWidth);
        setMinHeight(minHeight);

        Scene scene = new Scene(settingsScreen, minWidth, minHeight);
        scene.setFill(Color.TRANSPARENT);

        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(primaryStage);


        setHeader(header);
        setupDragging();
        setupResizing();

        draggablePanes.add(header);
        draggablePanes.add(settingsGradientBox);
        draggablePanes.add(titleText);
    }

    @Override
    public void handleClose() {
        log.info("Closing settings");
        super.handleClose();
        log.info("Settings closed");
    }
}
