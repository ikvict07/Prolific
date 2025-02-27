package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.stream.Collectors;


@Controller
@StageComponent("primaryStage")
@SuppressWarnings("unused")
@Getter
@Setter
public class ProjectsPanelController {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox content;
    private Stage stage;

    private FxmlProvider fxmlProvider;
    private UserSettingsHolder userSettingsHolder;

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

                    scrollPane.setOnScrollStarted(event -> vScrollBar.setOpacity(1));
                }
            }
        });

        addProjectPanels();
    }

    private void addProjectPanels() {
        var projects = userSettingsHolder.getUserProjects();
        projects.forEach(project -> {
            var title = project.getTitle();
            var icon = getIconTextFromTitle(title);
            var resource = fxmlProvider.getFxmlResource("projectPanel");
            ProjectPanelController controller = (ProjectPanelController) resource.getController();
            controller.getProjectTitleText().setText(title);
            controller.getProjectIconText().setText(icon);
            content.getChildren().add(resource.getParent());
            controller.init();
        });
    }

    private String getIconTextFromTitle(String title) {
        var splitted =  Arrays.stream(title.split("-")).toList();
        if (splitted.size() == 1) {
            return splitted.getFirst().substring(0,1).toUpperCase();
        } else {
            var result =  splitted.stream().map(s -> s.substring(0, 1)).collect(Collectors.joining()).toUpperCase();
            var firstLetter = result.substring(0,1);
            var lastLetter = result.substring(result.length()-1);
            return firstLetter + lastLetter;
        }
    }


    @Autowired
    private void set(FxmlProvider fxmlProvider, UserSettingsHolder userSettingsHolder) {
        this.fxmlProvider = fxmlProvider;
        this.userSettingsHolder = userSettingsHolder;
    }
}
