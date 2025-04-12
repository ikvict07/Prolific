package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsFooterController {
    @FXML
    public Label cancelButton;

    @Initialize
    public void initialize() {
        System.out.println(cancelButton);
    }
}
