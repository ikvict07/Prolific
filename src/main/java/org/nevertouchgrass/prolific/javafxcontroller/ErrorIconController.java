package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import org.springframework.stereotype.Component;

@Component
public class ErrorIconController {
    @FXML
    public Circle circle;
    @FXML
    public SVGPath svgPath1;
    @FXML
    public SVGPath svgPath2;
    @FXML
    public StackPane svgGraphic;
}
