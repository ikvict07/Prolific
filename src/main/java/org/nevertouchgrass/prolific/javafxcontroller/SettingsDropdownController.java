package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.service.scaners.PeriodicalScanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@StageComponent
@Log4j2
@SuppressWarnings("unused")
@Lazy
public class SettingsDropdownController {
    @FXML
    private Label settingsLabel;
    @FXML
    private Label scanLabel;
    @FXML
    private Label pluginsLabel;
    @Setter(onMethod_ = @Autowired)
    private PeriodicalScanningService periodicalScanningService;
    @Setter(onMethod_ = @Autowired)
    private SettingsHeaderController settingsHeaderController;

    public void rescan() {
        periodicalScanningService.rescan();
    }


    public void openSettings() {
        settingsHeaderController.open();
    }
}
