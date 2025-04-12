package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsScreenController {
    @FXML
    public AnchorPane settingsScreen;
}
