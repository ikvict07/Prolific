package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.nevertouchgrass.prolific.annotation.AnchorPaneController;
import org.nevertouchgrass.prolific.annotation.StageComponent;

@AnchorPaneController
@StageComponent("primaryStage")
public class MainScreenController {
	private Stage stage;

	@FXML
	public AnchorPane headerComponent;
}
