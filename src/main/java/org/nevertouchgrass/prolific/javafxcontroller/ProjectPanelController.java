package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
public class ProjectPanelController {
    @FXML
    private HBox projectPanel;
    @FXML
    private Text projectIconText;
    @FXML
    private Text projectTitleText;

}
