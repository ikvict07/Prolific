package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.components.LogsAndMetricsTextComponent;
import org.nevertouchgrass.prolific.components.MetricsChartComponent;
import org.nevertouchgrass.prolific.constants.action.SeeMetricsAction;
import org.nevertouchgrass.prolific.listener.InitializeAnnotationProcessor;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.nevertouchgrass.prolific.util.UIUtil.switchPaneChildren;

@Lazy
@Slf4j
@StageComponent
@SuppressWarnings("java:S1450")
public class LogsAndMetricsPanelController {
    @FXML
    private HBox placeForScrollPane;
    @FXML
    private HBox projectLogsDropdown;
    @FXML
    private Label chosenProject;
    @FXML
    private HBox foldButton;
    @FXML
    private Label logsButton;
    @FXML
    private Label metricsButton;
    @FXML
    private Label runningProjects;
    @FXML
    public Label chooseProjectFirst;

    private boolean isLogsOpened = true;

    private final ContextMenu contextMenu = new ContextMenu();
    @Setter(onMethod_ = @Autowired)
    private ProcessService processService;
    @Setter(onMethod_ = @Autowired)
    private ProcessLogsService processLogsService;
    @Setter(onMethod_ = @Autowired)
    private MetricsService metricsService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    private ObservableMap<Project, Set<ProcessWrapper>> processes;
    private SimpleIntegerProperty runningProjectsCount;

    private final Map<ProcessWrapper, LogsAndMetricsTextComponent> logsAndMetricsTextComponents = new HashMap<>();
    private ProcessWrapper currentProcess;
    private StringProperty projectChoice;
    @Setter(onMethod_ = @Autowired)
    private PermissionRegistry permissionRegistry;

    /**
     * Will be called by InitializeAnnotationProcessor
     *
     * @see InitializeAnnotationProcessor
     */
    @Initialize
    @SuppressWarnings("unused")
    public void init() {
        projectChoice = localizationProvider.choose_project_first();
        chosenProject.textProperty().bind(projectChoice);
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());
        runningProjects.textProperty().set(localizationProvider.running_projects_count().get());

        processes = processService.getObservableLiveProcesses();
        var deadProcesses = processService.getObservableDeadProcesses();

        List<CustomMenuItem> liveProcessMenuItems = new ArrayList<>();
        List<CustomMenuItem> deadProcessMenuItems = new ArrayList<>();

        var delimiter = new MenuItem();
        delimiter.textProperty().bind(localizationProvider.dead());
        delimiter.setDisable(true);

        runningProjectsCount = new SimpleIntegerProperty(processes.size());
        runningProjectsCount.addListener((_, oldValue, newValue) -> Platform.runLater(() ->
                runningProjects.textProperty().set(localizationProvider.running_projects_count()
                        .get()
                        .replaceAll("\\d+", String.valueOf(newValue)))));

        localizationProvider.running_projects_count().addListener((_, oldValue, newValue) -> Platform.runLater(() ->
                runningProjects.textProperty().set(newValue.replaceAll("\\d+", String.valueOf(runningProjectsCount.getValue())))));

        runningProjects.textProperty().bindBidirectional(localizationProvider.running_projects_count());

        processes.addListener((MapChangeListener<? super Project, ? super Set<ProcessWrapper>>) change -> {
            if (change.wasAdded()) {
                CustomMenuItem menuItem = createMenuItemForProcess(change.getKey(), change.getValueAdded());
                liveProcessMenuItems.add(menuItem);
            } else if (change.wasRemoved()) {
                liveProcessMenuItems.removeIf(item -> item.getId().equals(change.getKey().getTitle()));
            }
            refreshContextMenu(liveProcessMenuItems, deadProcessMenuItems, delimiter);
            runningProjectsCount.set(processes.size());
        });

        deadProcesses.addListener((MapChangeListener<? super Project, ? super Set<ProcessWrapper>>) change -> {
            if (change.wasAdded()) {
                CustomMenuItem menuItem = createMenuItemForProcess(change.getKey(), change.getValueAdded());
                menuItem.setId(change.getKey().getTitle() + "_dead");
                deadProcessMenuItems.add(menuItem);
            } else if (change.wasRemoved()) {
                deadProcessMenuItems.removeIf(item -> item.getId().equals(change.getKey().getTitle() + "_dead"));
            }
            refreshContextMenu(liveProcessMenuItems, deadProcessMenuItems, delimiter);
        });
    }

    private CustomMenuItem createMenuItemForProcess(Project project, Set<ProcessWrapper> processWrappers) {
        CustomMenuItem menuItem = new CustomMenuItem(new Label(project.getTitle()));
        menuItem.setId(project.getTitle());
        menuItem.setHideOnClick(false);

        if (processWrappers.size() == 1) {
            ProcessWrapper processWrapper = processWrappers.iterator().next();
            setupProcessMenuItem(menuItem, project, project.getTitle() + " - " + processWrapper.getName(), processWrapper);
            menuItem.fire();
        } else {
            menuItem.addEventHandler(ActionEvent.ACTION, _ -> {
                projectChoice.unbind();
                projectChoice.set(project.getTitle());

                ContextMenu subMenu = new ContextMenu();

                for (ProcessWrapper processWrapper : processWrappers) {
                    CustomMenuItem item = new CustomMenuItem();
                    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                    setupProcessMenuItem(item, project, processWrapper.getName() + " - " + processWrapper.getTerminalTime().format(timeFormatter), processWrapper);
                    subMenu.getItems().add(item);
                }

                Bounds bounds = menuItem.getStyleableNode().localToScreen(menuItem.getStyleableNode().getBoundsInLocal());
                subMenu.show(menuItem.getParentPopup(), bounds.getMaxX(), bounds.getMinY());
            });
        }
        return menuItem;
    }

    private void refreshContextMenu(List<CustomMenuItem> liveItems, List<CustomMenuItem> deadItems, MenuItem delimiter) {
        Platform.runLater(() -> {
            contextMenu.getItems().clear();
            contextMenu.getItems().addAll(liveItems);
            if (!deadItems.isEmpty()) {
                contextMenu.getItems().add(delimiter);
                contextMenu.getItems().addAll(deadItems);
            }
        });
    }


    public void showRunningProjects() {
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        } else {
            Bounds bounds = projectLogsDropdown.localToScreen(projectLogsDropdown.getBoundsInLocal());
            contextMenu.show(projectLogsDropdown, bounds.getMinX(), bounds.getMaxY());
        }
    }

    private void switchConfigurationButtonIcon() {
        String resource = "unfoldButton".equalsIgnoreCase(foldButton.getChildren().getFirst().getId()) ? "/icons/fxml/fold_button.fxml" : "/icons/fxml/unfold_button.fxml";
        switchPaneChildren(foldButton, resource);
    }

    public void switchLogsButtonStyle(MouseEvent event) {
        String style = "selected";
        if (event.getSource() == logsButton) {
            ObservableList<String> styleClass = logsButton.getStyleClass();
            if (!styleClass.contains(style)) {
                logsButton.getStyleClass().add(style);
            }
            metricsButton.getStyleClass().remove(style);
            isLogsOpened = true;
        } else if (event.getSource() == metricsButton) {
            ObservableList<String> styleClass = metricsButton.getStyleClass();
            if (!styleClass.contains(style)) {
                metricsButton.getStyleClass().add(style);
            }
            logsButton.getStyleClass().remove(style);
            isLogsOpened = false;
        }
        if (currentProcess != null) {
            changeLogs(currentProcess);
        }
    }

    private void setupProcessMenuItem(@NonNull CustomMenuItem menuItem, Project project, String text, ProcessWrapper processWrapper) {
        menuItem.setContent(new Label(text));
        menuItem.setHideOnClick(false);
        menuItem.setOnAction(_ -> Platform.runLater(() -> {
            projectChoice.unbind();
            projectChoice.set(project.getTitle() + " - " + processWrapper.getName());
            currentProcess = processWrapper;
            changeLogs(processWrapper);
        }));
    }

    private final Map<ProcessWrapper, MetricsChartComponent> metricsComponents = new HashMap<>();


    private void changeLogs(ProcessWrapper processWrapper) {
        placeForScrollPane.getChildren().clear();
        var processLogs = processLogsService.getLogs().getOrDefault(processWrapper, new ProcessLogs());
        var flux = processLogsService.subscribeToLogs(processWrapper);

        var logComponent = logsAndMetricsTextComponents.computeIfAbsent(processWrapper,
                _ -> {
                    var componentProvider = new LogsAndMetricsTextComponent(processLogs, flux);
                    componentProvider.init();
                    return componentProvider;
                });
        if (permissionRegistry.getChecker(SeeMetricsAction.class).hasPermission(new SeeMetricsAction())) {
            var metricsComponent = metricsComponents.computeIfAbsent(processWrapper,
                    _ -> new MetricsChartComponent(metricsService, processWrapper));
            if (!isLogsOpened) {
                placeForScrollPane.getChildren().add(metricsComponent);
            }
        }
        if (isLogsOpened) {
            placeForScrollPane.getChildren().add(logComponent.getComponent());
        }
    }
}
