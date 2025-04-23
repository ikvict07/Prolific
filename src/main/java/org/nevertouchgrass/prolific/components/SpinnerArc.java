package org.nevertouchgrass.prolific.components;

import javafx.animation.RotateTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;
import lombok.Getter;

public class SpinnerArc extends StackPane {
    @Getter
    private int radius = 4;
    private final Arc arc;

    public SpinnerArc() {
        arc = new Arc(12, 12, radius, radius, 0, 270);
        arc.setType(ArcType.OPEN);
        arc.setStrokeWidth(2);
        arc.setStrokeLineCap(StrokeLineCap.ROUND);
        arc.setFill(null);

        arc.setStroke(new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#6C707E")),
                new Stop(1, Color.TRANSPARENT)
        ));

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), arc);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.play();

        getChildren().add(arc);
    }

    public void setRadius(int radius) {
        this.radius = radius;
        arc.setRadiusX(radius);
        arc.setRadiusY(radius);
    }
}
