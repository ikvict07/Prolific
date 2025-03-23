package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.nevertouchgrass.prolific.service.ProjectScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CancellingDropdownController {
    @FXML
    public Label cancelButton;
    @FXML
    public VBox root;

    private ProjectScannerService projectScannerService;

    @Autowired
    public void setProjectScannerService(ProjectScannerService projectScannerService) {
        this.projectScannerService = projectScannerService;
    }

    public void cancel() {
        projectScannerService.cancelScanning();
    }
}
