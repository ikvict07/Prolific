package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import org.springframework.stereotype.Component;

@Component
public class StarController {
    @FXML
    public SVGPath star;
    @FXML
    public StackPane pane;
}
