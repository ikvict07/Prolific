package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.OnDelete;
import org.nevertouchgrass.prolific.annotation.OnSave;
import org.nevertouchgrass.prolific.annotation.OnUpdate;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.Comparator;

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
    @FXML
    private Region upperShadow;
    @FXML
    private Region lowerShadow;

    private FxmlProvider fxmlProvider;
    private UserSettingsHolder userSettingsHolder;
    private ProjectsRepository projectsRepository;

    private final ObservableList<Project> projects = FXCollections.observableArrayList();
    private final Comparator<Project> projectComparator = Comparator
            .comparing(Project::getIsStarred).reversed()
            .thenComparing(p -> p.getTitle().toLowerCase());

    @Initialize
    private void init() {
        content.minWidthProperty().bind(scrollPane.widthProperty());
        content.prefWidthProperty().bind(scrollPane.widthProperty());
        content.maxWidthProperty().bind(scrollPane.widthProperty());
        setupScrollBarFadeEffect();
        projectsRepository.findAll(Project.class).forEach(this::addProjectToList);

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> handleShadow(lowerShadow, newValue.doubleValue(), false));

        scrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> handleShadow(upperShadow, newValue.doubleValue(), true));


    }

    private void handleShadow(Region shadow, double newValue, boolean isUpperShadow) {
        try {
            double elementHeight;
            double halfScroll;

            if (isUpperShadow) {
                elementHeight = content.getChildren().getFirst().getBoundsInLocal().getHeight();
                halfScroll = elementHeight / (content.getChildren().size() * elementHeight);

                if (newValue <= halfScroll) {
                    double shadowHeight = (newValue / halfScroll) * (elementHeight / 2);
                    shadowHeight = Math.max(0, Math.min(elementHeight / 2, shadowHeight));

                    shadow.setPrefHeight(shadowHeight);
                    shadow.setMinHeight(shadowHeight);
                    shadow.setMaxHeight(shadowHeight);
                } else {
                    double maxShadowHeight = elementHeight / 2;
                    shadow.setPrefHeight(maxShadowHeight);
                    shadow.setMinHeight(maxShadowHeight);
                    shadow.setMaxHeight(maxShadowHeight);
                }

                shadow.setVisible(true);

            } else {
                elementHeight = content.getChildren().getLast().getBoundsInLocal().getHeight();
                halfScroll = 1.0 - (elementHeight / (content.getChildren().size() * elementHeight));

                if (newValue < halfScroll) {
                    double maxShadowHeight = elementHeight / 2;
                    shadow.setPrefHeight(maxShadowHeight);
                    shadow.setMinHeight(maxShadowHeight);
                    shadow.setMaxHeight(maxShadowHeight);
                } else {
                    double shadowHeight = ((1.0 - newValue) / (1.0 - halfScroll)) * (elementHeight / 2);
                    shadowHeight = Math.max(0, Math.min(elementHeight / 2, shadowHeight));

                    shadow.setPrefHeight(shadowHeight);
                    shadow.setMinHeight(shadowHeight);
                    shadow.setMaxHeight(shadowHeight);
                }

                shadow.setVisible(newValue < 1.0);
            }
        } catch (Exception _) {
            // ignore
        }
    }


    private void setupScrollBarFadeEffect() {
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
    }

    @OnSave(Project.class)
    private void addProjectToList(Project project) {
        Platform.runLater(() -> {
            if (!projects.contains(project)) {
                int index = findInsertionIndex(project);
                projects.add(index, project);
                insertProjectPanelAt(index, project);
            }
        });
    }

    @OnDelete(Project.class)
    private void deleteProjectFromList(Project project) {
        Platform.runLater(() -> {
            int index = projects.indexOf(project);
            if (index != -1) {
                projects.remove(index);
                content.getChildren().remove(index);
            }
        });
    }

    @OnUpdate(Project.class)
    public void updateProject(Project project) {
        Platform.runLater(() -> {
            int oldIndex = projects.indexOf(project);
            if (oldIndex != -1) {
                projects.remove(oldIndex);
                content.getChildren().remove(oldIndex);
            }
            int newIndex = findInsertionIndex(project);
            projects.add(newIndex, project);
            insertProjectPanelAt(newIndex, project);
        });
    }

    private int findInsertionIndex(Project project) {
        var index = Collections.binarySearch(projects, project, projectComparator);
        return index < 0 ? -index - 1 : index;
    }

    private void insertProjectPanelAt(int index, Project project) {
        var resource = fxmlProvider.getFxmlResource("projectPanel");
        ProjectPanelController controller = (ProjectPanelController) resource.getController();
        controller.getProjectTitleText().setText(project.getTitle());
        controller.getProjectIconText().setText(getIconTextFromTitle(project.getTitle()));
        controller.setProject(project);
        content.getChildren().add(index, resource.getParent());
        controller.init();
    }

    private String getIconTextFromTitle(String title) {
        var parts = title.split("-");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        } else {
            return (parts[0].charAt(0) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }

    @Autowired
    private void set(FxmlProvider fxmlProvider, UserSettingsHolder userSettingsHolder, ProjectsRepository projectsRepository) {
        this.fxmlProvider = fxmlProvider;
        this.userSettingsHolder = userSettingsHolder;
        this.projectsRepository = projectsRepository;
    }
}