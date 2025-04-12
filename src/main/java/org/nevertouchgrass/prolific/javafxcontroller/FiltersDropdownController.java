package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;

@StageComponent
public class FiltersDropdownController {

    @FXML
    public Label isStared;
    @FXML
    public Label isManuallyAdded;

    @Initialize
    public void initialize() {
    }
}
