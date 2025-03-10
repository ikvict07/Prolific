package org.nevertouchgrass.prolific.service.icons;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultProjectTypeIconFactory extends AbstractIconFactory {
    public DefaultProjectTypeIconFactory() {
        super("default");
    }

    @Override
    public StackPane configure() {
        StackPane stackPane = new StackPane();

        Rectangle rectangle = new Rectangle();
        rectangle.setX(1.5);
        rectangle.setY(2.5);
        rectangle.setWidth(13);
        rectangle.setHeight(11);
        rectangle.setArcWidth(3);
        rectangle.setArcHeight(3);
        rectangle.setFill(Color.web("#43454A"));
        rectangle.setStroke(Color.web("#CED0D6"));
        StackPane.setAlignment(rectangle, Pos.CENTER);
        rectangle.setScaleX(1.5);
        rectangle.setScaleY(1.5);
        SVGPath arrowLeft = new SVGPath();
        arrowLeft.setContent("M4 6L6 8L4 10");
        arrowLeft.setStroke(Color.web("#CED0D6"));
        arrowLeft.setStrokeWidth(1.0);
        arrowLeft.setStrokeLineCap(StrokeLineCap.ROUND);
        arrowLeft.setFill(null);
        StackPane.setAlignment(arrowLeft, Pos.CENTER);
        arrowLeft.setTranslateX(-3);
        arrowLeft.setScaleX(1.5);
        arrowLeft.setScaleY(1.5);

        SVGPath lineRight = new SVGPath();
        lineRight.setContent("M7.5 10.5H11.5");
        lineRight.setStroke(Color.web("#CED0D6"));
        lineRight.setStrokeWidth(1.0);
        lineRight.setStrokeLineCap(StrokeLineCap.ROUND);
        lineRight.setFill(null);
        StackPane.setAlignment(lineRight, Pos.CENTER);
        lineRight.setTranslateX(2);
        lineRight.setTranslateY(4);
        lineRight.setScaleX(1.5);
        lineRight.setScaleY(1.5);

        stackPane.getChildren().addAll(rectangle, arrowLeft, lineRight);

        return stackPane;
    }
}
