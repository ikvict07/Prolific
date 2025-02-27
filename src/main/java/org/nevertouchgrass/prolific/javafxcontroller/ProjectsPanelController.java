package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.stereotype.Controller;


@Controller
@StageComponent("primaryStage")
public class ProjectsPanelController {

    @FXML
    public ScrollPane scrollPane;


    private Stage stage;

    @Initialize
    private void init() {
        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar vScrollBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");
                if (vScrollBar != null) {
                    FadeTransition fadeOutV = new FadeTransition(Duration.seconds(1), vScrollBar);
                    vScrollBar.setOpacity(0);
                    fadeOutV.setToValue(0);
                    fadeOutV.setInterpolator(Interpolator.EASE_OUT);
                    scrollPane.setOnScroll(event -> {
                        vScrollBar.setOpacity(1);

                        fadeOutV.stop();
                        fadeOutV.setFromValue(1);

                        fadeOutV.playFromStart();
                    });

                    scrollPane.setOnScrollStarted(event -> {
                        vScrollBar.setOpacity(1);
                    });
                }
            }
        });

    }

}
