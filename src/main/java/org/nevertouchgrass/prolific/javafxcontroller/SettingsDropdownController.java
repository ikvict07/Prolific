package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.service.scaners.PeriodicalScanningService;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.awt.*;
import java.io.File;
import java.io.IOException;

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
    @Setter(onMethod_  = @Autowired)
    private PathService pathService;

    public void rescan() {
        periodicalScanningService.rescan();
    }


    public void openSettings() {
        settingsHeaderController.open();
    }

    public void openPluginsDir() {
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    File file = pathService.getPluginsPath().toFile();
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    }
                } else {
                    log.error("Desktop is not supported");
                }
            } catch (IOException e) {
                log.error("Error opening file in explorer: {}", e.getMessage());
            }
        }).start();
    }
}
