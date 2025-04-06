package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import org.nevertouchgrass.prolific.localization.LocalizationBinding;
import org.nevertouchgrass.prolific.localization.LocalizationManager;
import org.nevertouchgrass.prolific.service.PeriodicalScanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.scene.control.Label;
import java.util.Locale;

@Component
public class SettingsDropdownController {
    @FXML
    private Label settingsLabel;
    @FXML
    private Label scanLabel;
    @FXML
    private Label pluginsLabel;

    @Autowired
    private LocalizationBinding localizationBinding;

    @Autowired
    private LocalizationManager localizationManager;

    private PeriodicalScanningService periodicalScanningService;

    @FXML
    public void initialize() {
        settingsLabel.textProperty().bind(localizationBinding.settingsProperty());
        scanLabel.textProperty().bind(localizationBinding.scanProperty());
        pluginsLabel.textProperty().bind(localizationBinding.pluginsProperty());
    }


    @Autowired
    public void set(PeriodicalScanningService periodicalScanningService) {
        this.periodicalScanningService = periodicalScanningService;
    }

    public void rescan() {
        periodicalScanningService.rescan();
    }

    public void changeLanguage() {
        localizationManager.setLocale(Locale.forLanguageTag("sk"));
        localizationBinding.updateLanguage();
        System.out.println("Updated settings text: " + settingsLabel.getText());
    }
}
