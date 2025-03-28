package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.ProjectsService;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.service.searching.comparators.ProjectComparatorBuilder;
import org.nevertouchgrass.prolific.service.searching.filters.ProjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Predicate;

@Controller
@SuppressWarnings("unused")
@Getter
@Setter
public class ProjectsPanelController {
    public static final String PROJECT_KEY = "project";
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox content;
    @FXML
    private Region upperShadow;
    @FXML
    private Region lowerShadow;

    private FxmlProvider fxmlProvider;
    private UserSettingsHolder userSettingsHolder;
    private ProjectsRepository projectsRepository;
    private ProjectsService projectsService;

    private Predicate<Project> filterFunction = ProjectFilterService.getDefaultFilter();
    private Comparator<Project> projectComparator = ProjectComparatorBuilder.getDefault();
    private ProcessService processService;

    @Initialize
    private void init() {
        registerListeners();
        projectsService.getProjects().forEach(this::addProjectToList);
        content.minWidthProperty().bind(scrollPane.widthProperty());
        content.prefWidthProperty().bind(scrollPane.widthProperty());
        content.maxWidthProperty().bind(scrollPane.widthProperty());
        setupScrollBarFadeEffect();

        upperShadow.visibleProperty().bind(scrollPane.vvalueProperty().greaterThan(0));
        lowerShadow.visibleProperty().bind(scrollPane.vvalueProperty().lessThan(1));
    }

    private void registerListeners() {
        projectsService.registerOnAddListener(this::addProjectToList);
        projectsService.registerOnRemoveListener(this::deleteProjectFromList);
        projectsService.registerOnUpdateListener(this::updateProject);
    }

    public void filterProjects(Predicate<Project> filterFunction) {
        this.filterFunction = filterFunction;
        updateContent();
    }

    public void changeComparator(Comparator<Project> comparator) {
        projectComparator = comparator;
        updateContent();
    }

    private void updateContent() {
        content.getChildren().clear();
        projectsService.getProjects().forEach(this::addProjectToList);
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


    private void addProjectToList(Project project) {
        if (!filterFunction.test(project)) return;
        Platform.runLater(() -> {
            int index = findInsertionIndex(project);
            insertProjectPanelAt(index, project);
        });
    }

    private void deleteProjectFromList(Project project) {
        Platform.runLater(() -> {
            var toDelete = content.getChildren().filtered(node -> node.getProperties().get(PROJECT_KEY).equals(project));
            content.getChildren().removeAll(toDelete);
        });
    }

    public void updateProject(Project project) {
        Platform.runLater(() -> {
            var toDelete = content.getChildren().filtered(node -> node.getProperties().get(PROJECT_KEY).equals(project));
            content.getChildren().removeAll(toDelete);
            var newIndex = findInsertionIndex(project);
            insertProjectPanelAt(newIndex, project);
        });
    }

    private int findInsertionIndex(Project project) {
        var index = Collections.binarySearch(content.getChildren().stream().map(node -> node.getProperties().get(PROJECT_KEY)).filter(Objects::nonNull).map(p -> (Project) p).toList(), project, projectComparator);
        return index < 0 ? -index - 1 : index;
    }

    private void insertProjectPanelAt(int index, Project project) {
        var resource = fxmlProvider.getFxmlResource("projectPanel");
        ProjectPanelController controller = (ProjectPanelController) resource.getController();
        controller.getProjectTitleText().setText(project.getTitle());
        controller.getProjectIconText().setText(getIconTextFromTitle(project.getTitle()));
        controller.setProject(project);
        var parent = resource.getParent();
        parent.getProperties().put(PROJECT_KEY, project);
        content.getChildren().add(index, parent);
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
    private void set(FxmlProvider fxmlProvider, UserSettingsHolder userSettingsHolder, ProjectsRepository projectsRepository, ProjectsService projectsService, ProcessService processService) {
        this.fxmlProvider = fxmlProvider;
        this.userSettingsHolder = userSettingsHolder;
        this.projectsRepository = projectsRepository;
        this.projectsService = projectsService;
        this.processService = processService;
    }
}