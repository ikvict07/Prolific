package org.nevertouchgrass.prolific.javafxcontroller;

import org.nevertouchgrass.prolific.service.scaners.PeriodicalScanningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsDropdownController {

    private PeriodicalScanningService periodicalScanningService;

    @Autowired
    public void set(PeriodicalScanningService periodicalScanningService) {
        this.periodicalScanningService = periodicalScanningService;
    }


    public void rescan() {
        periodicalScanningService.rescan();
    }
}
