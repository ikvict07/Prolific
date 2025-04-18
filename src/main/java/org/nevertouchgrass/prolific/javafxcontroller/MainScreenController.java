package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

@Lazy
@Slf4j
@StageComponent
@SuppressWarnings("unused")
public class MainScreenController {
    @Setter(onMethod_ = {@Qualifier("primaryStage"), @Autowired})
    private Stage stage;

    @FXML
    public AnchorPane mainScreen;
    @FXML
    public VBox logsAndMetricsPanel;

    @Initialize
    public void init() {
        ChangeListener<Number> block1 = (observable, oldValue, newValue) -> mainScreen.setPrefWidth(newValue.doubleValue());
        ChangeListener<Number> block2 = (observable, oldValue, newValue) -> mainScreen.setPrefHeight(newValue.doubleValue());
        stage.widthProperty().addListener(block1);
        stage.heightProperty().addListener(block2);
    }
}
