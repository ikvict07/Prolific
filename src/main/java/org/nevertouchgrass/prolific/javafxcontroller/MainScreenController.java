package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.nevertouchgrass.prolific.annotation.AnchorPaneController;
import org.nevertouchgrass.prolific.annotation.ConstraintsIgnoreElementSize;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;

@AnchorPaneController
@StageComponent("primaryStage")
@SuppressWarnings("unused")
public class MainScreenController {
    @FXML
    @ConstraintsIgnoreElementSize(left = 0.05, top = 0.20, right = 0.5)
    public ScrollPane projectsPanel;
    private Stage stage;

    @FXML
    public AnchorPane headerComponent;

    @FXML
    public AnchorPane mainScreen;


    @Initialize
    public void init() {
        ChangeListener<Number> block1 = (observable, oldValue, newValue) -> mainScreen.setPrefWidth(newValue.doubleValue());
        ChangeListener<Number> block2 = (observable, oldValue, newValue) -> mainScreen.setPrefHeight(newValue.doubleValue());
        stage.widthProperty().addListener(block1);
        stage.heightProperty().addListener(block2);
    }
}
