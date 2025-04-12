package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsFooterController {
    @FXML
    public Label cancelButton;
    @FXML
    public Label applyButton;
    @FXML
    public Label submitButton;
    @FXML public AnchorPane settingsFooter;
    @FXML public HBox content;

    @Setter(onMethod_ = @Autowired)
    private SettingsHeaderController settingsHeaderController;

    @Initialize
    public void initialize() {
    }

    public void cancel() {
        settingsHeaderController.handleClose();
    }

    public void apply() {}

    public void submit() {
        settingsHeaderController.handleClose();
    }
}
