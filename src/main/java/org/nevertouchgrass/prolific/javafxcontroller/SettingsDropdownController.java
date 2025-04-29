package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.nevertouchgrass.prolific.service.PeriodicalScanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SettingsDropdownController {

    private PeriodicalScanningService periodicalScanningService;
    @Autowired
    public void setPeriodicalScanningService(PeriodicalScanningService periodicalScanningService) {
        this.periodicalScanningService = periodicalScanningService;
    }
    @FXML
    public void showLicenseWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/components/licenseWindow.fxml"));
            AnchorPane licensePane = loader.load();

            LicenseController licenseController = loader.getController();

            Stage stage = new Stage();
            stage.setTitle("License Activation");
            stage.setScene(new Scene(licensePane));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void rescan() {
        periodicalScanningService.rescan();
    }
}
