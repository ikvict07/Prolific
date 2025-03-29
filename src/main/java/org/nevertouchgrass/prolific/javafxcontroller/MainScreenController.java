package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StageComponent("primaryStage")
@SuppressWarnings("unused")
public class MainScreenController {
    @FXML
    public StackPane projectsPanel;
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
