package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@StageComponent(stage = "settingsStage")
public class SettingsListController {
    @Setter(onMethod_ = {@Qualifier("settingsStage"), @Autowired})
    private Stage settingsStage;

    @FXML public Label rootPath;
    @FXML public Label excludedDirs;
    @FXML public Label maxScanDepth;
    @FXML public Label rescanEveryHours;
    @FXML public Label language;

    @FXML
    public void initialize() {
    }

    public void handleMouseEntered() {
        settingsStage.getScene().setCursor(Cursor.DEFAULT);
    }
}
