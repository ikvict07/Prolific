package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.nevertouchgrass.prolific.licensing.LicenseActivationWindow;
import org.nevertouchgrass.prolific.licensing.LicenseManager;
import org.nevertouchgrass.prolific.service.PeriodicalScanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.stage.Stage;
import java.awt.*;

@Component
public class SettingsDropdownController {
    @Autowired
    private LicenseActivationWindow licenseActivationWindow;

    private PeriodicalScanningService periodicalScanningService;
    @Autowired
    public void set(PeriodicalScanningService periodicalScanningService) {
        this.periodicalScanningService = periodicalScanningService;
    }

    public SettingsDropdownController() {
        Button acceptButton = new Button("Accept License");
        acceptButton.setOnAction(event -> showLicenseWindow());
    }

    @FXML
    private void showLicenseWindow() {
        if (licenseActivationWindow.getIsActivated()) {
            return;
        }
        Stage stage = new Stage();
        LicenseManager.getGeneratedKeys();
        licenseActivationWindow.showLicenseWindow(stage);
    }

    public void rescan() {
        periodicalScanningService.rescan();
    }
}
