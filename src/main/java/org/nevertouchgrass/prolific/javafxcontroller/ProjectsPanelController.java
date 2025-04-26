package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.ProjectsService;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.service.searching.comparators.ProjectComparatorBuilder;
import org.nevertouchgrass.prolific.service.searching.filters.ProjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

@StageComponent
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
    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;
    @Setter(onMethod_ = @Autowired)
    private UserSettingsHolder userSettingsHolder;
    @Setter(onMethod_ = @Autowired)
    private ProjectsRepository projectsRepository;
    @Setter(onMethod_ = @Autowired)
    private ProjectsService projectsService;
    @Setter(onMethod_ = @Autowired)
    private ProcessService processService;

    private Predicate<Project> filterFunction = ProjectFilterService.getDefaultFilter();
    private Comparator<Project> projectComparator = ProjectComparatorBuilder.getDefault();

    private List<Node> contentChildren;

    @Initialize
    private void init() {
        registerListeners();
        var futures = projectsService.getProjects().stream().map(this::addProjectToList).toList();
        content.minWidthProperty().bind(scrollPane.widthProperty());
        content.prefWidthProperty().bind(scrollPane.widthProperty());
        content.maxWidthProperty().bind(scrollPane.widthProperty());
        setupScrollBarFadeEffect();

        upperShadow.visibleProperty().bind(scrollPane.vvalueProperty().greaterThan(0));
        scrollPane.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                ScrollBar vScrollBar = (ScrollBar) scrollPane.lookup(".scroll-bar:vertical");
                if (vScrollBar != null) {
                    lowerShadow.visibleProperty().bind(
                            vScrollBar.visibleProperty().and(scrollPane.vvalueProperty().lessThan(1))
                    );
                }
            }
        });
        scrollPane.requestFocus();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> contentChildren = new ArrayList<>(content.getChildren()));
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
        contentChildren.forEach(child -> {
            var project = (Project) child.getProperties().get(PROJECT_KEY);
            if (project != null && filterFunction.test(project)) {
                var pos = findInsertionIndex(project);
                var freshContentChildren = content.getChildren();
                if (!freshContentChildren.stream().map(node -> ((Project) node.getProperties().get(PROJECT_KEY)).getPath()).toList().contains(project.getPath())) {
                    freshContentChildren.add(pos, child);
                }
            }
        });
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


    private List<Node> getProjectPanels() {
        return content.getChildren().filtered(node -> node.getProperties().get(PROJECT_KEY) != null);
    }

    private CompletableFuture<Void> addProjectToList(Project project) {
        var projectPanel = generateProjectPanel(project);
        if (contentChildren != null && !contentChildren.stream()
                .map(node -> ((Project) node.getProperties().get(PROJECT_KEY)).getPath())
                .toList().contains(project.getPath())) {
            contentChildren.add(projectPanel);
        }
        if (!filterFunction.test(project)) return CompletableFuture.completedFuture(null);
        CompletableFuture<Void> uiUpdateFuture = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                int index = findInsertionIndex(project);
                content.getChildren().add(index, projectPanel);
                uiUpdateFuture.complete(null);
            } catch (Exception e) {
                uiUpdateFuture.completeExceptionally(e);
            }
        });
        return uiUpdateFuture;
    }

    private void deleteProjectFromList(Project project) {
        Platform.runLater(() -> {
            try {
                var toDelete = content.getChildren().filtered(node -> (((Project) node.getProperties().get(PROJECT_KEY)).getPath()).equals(project.getPath()));
                content.getChildren().removeAll(toDelete);
                contentChildren.removeIf(n -> n.getProperties().get(PROJECT_KEY) != null && ((Project) n.getProperties().get(PROJECT_KEY)).getPath().equals(project.getPath()));
            } catch (Exception e) {
                // Ignore
            }
        });
    }

    public void updateProject(Project project) {
        Platform.runLater(() -> {
            var toUpdate = content.getChildren().stream().filter(node -> ((Project) node.getProperties().get(PROJECT_KEY)).getId().equals(project.getId())).toList();
            content.getChildren().removeAll(toUpdate);
            var newIndex = findInsertionIndex(project);
            ((ProjectPanelController) toUpdate.getFirst().getProperties().get("controller")).updateStar();
            content.getChildren().add(newIndex, toUpdate.getFirst());
        });

    }

    private Parent generateProjectPanel(Project project) {
        var resource = fxmlProvider.getFxmlResource("projectPanel");
        ProjectPanelController controller = (ProjectPanelController) resource.getController();
        controller.getProjectTitleText().setText(project.getTitle());
        controller.getProjectIconText().setText(getIconTextFromTitle(project.getTitle()));
        controller.setProject(project);
        var parent = resource.getParent();
        parent.getProperties().put(PROJECT_KEY, project);
        parent.getProperties().put("controller", controller);
        controller.addUpdateListener(this::updateProject);
        return parent;
    }

    private int findInsertionIndex(Project project) {
        var index = Collections.binarySearch(content.getChildren().stream().map(node -> node.getProperties().get(PROJECT_KEY)).filter(Objects::nonNull).map(p -> (Project) p).toList(), project, projectComparator);
        return index < 0 ? -index - 1 : index;
    }

    private String getIconTextFromTitle(String title) {
        var parts = title.split("-");
        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        } else {
            return (parts[0].charAt(0) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
        }
    }
}