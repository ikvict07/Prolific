package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.nevertouchgrass.prolific.annotation.AnchorPaneController;
import org.nevertouchgrass.prolific.annotation.Constraints;
import org.nevertouchgrass.prolific.annotation.ConstraintsIgnoreElementSize;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;

@AnchorPaneController
@StageComponent("primaryStage")
public class MainScreenController {
	private Stage stage;

	@FXML
	public AnchorPane headerComponent;

	@FXML
	public AnchorPane mainScreen;

	@FXML
	@ConstraintsIgnoreElementSize(top = 0.20, left = 0.05, right = 0.5)
	public ScrollPane projectsPanel;


	@Initialize
	public void init() {
		var scene = stage.getScene();
		ChangeListener<Number> block1 = (observable, oldValue, newValue) -> {
			mainScreen.setPrefWidth(newValue.doubleValue());
		};
		ChangeListener<Number> block2 = (observable, oldValue, newValue) -> {
			mainScreen.setPrefHeight(newValue.doubleValue());
		};
		scene.widthProperty().addListener(block1);
		scene.heightProperty().addListener(block2);
		mainScreen.setPrefWidth(scene.getWidth());
		mainScreen.setPrefHeight(scene.getHeight());
	}
}
