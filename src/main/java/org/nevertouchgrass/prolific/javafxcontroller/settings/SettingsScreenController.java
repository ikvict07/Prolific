package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsScreenController {
    @FXML
    public BorderPane settingsScreen;
    @FXML
    public AnchorPane settingsHeader;
    @FXML
    public SplitPane settingsList;
    @FXML
    public AnchorPane settingsFooter;
}
