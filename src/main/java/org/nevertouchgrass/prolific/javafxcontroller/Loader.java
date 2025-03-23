package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.springframework.stereotype.Component;

@Component
public class Loader {
    public Pane createLoader() {
        double scale = 1.5;

        double totalSize = 14.0 * scale;
        Pane container = new Pane();
        container.setPrefSize(totalSize, totalSize);
        container.setMinSize(totalSize, totalSize);
        container.setMaxSize(totalSize, totalSize);

        Group spinnerGroup = new Group();

        double rectWidth = 2.0 * scale;
        double rectHeight = 4.0 * scale;
        double radius = 5.0 * scale;

        double[] opacities = {1.0, 0.3, 0.38, 0.48, 0.62, 0.69, 0.78, 0.93};

        for (int i = 0; i < 8; i++) {
            Rectangle rect = new Rectangle(rectWidth, rectHeight, Color.web("#6F737A"));
            rect.setArcWidth(2.0 * scale);
            rect.setArcHeight(2.0 * scale);
            rect.setOpacity(opacities[i]);

            rect.setX(-rectWidth / 2);
            rect.setY(-rectHeight / 2);

            Group rectGroup = new Group(rect);

            double angleDegrees = i * 45.0;
            rectGroup.getTransforms().add(new Rotate(angleDegrees));

            rectGroup.setTranslateX(Math.sin(Math.toRadians(angleDegrees)) * radius);
            rectGroup.setTranslateY(-Math.cos(Math.toRadians(angleDegrees)) * radius);

            spinnerGroup.getChildren().add(rectGroup);
        }

        Rotate rotate = new Rotate();
        rotate.setPivotX(0);
        rotate.setPivotY(0);
        spinnerGroup.getTransforms().add(rotate);

        spinnerGroup.setLayoutX(totalSize / 2);
        spinnerGroup.setLayoutY(totalSize / 2);

        container.getChildren().add(spinnerGroup);

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1.5), spinnerGroup);
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.play();

        return container;
    }
}