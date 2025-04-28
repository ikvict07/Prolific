package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.components.LogsAndMetricsTextComponent;
import org.nevertouchgrass.prolific.components.MetricsChartComponent;
import org.nevertouchgrass.prolific.listener.InitializeAnnotationProcessor;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.TerminatedProcessInfo;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    @Autowired
    private ProcessService processService;
    @Setter(onMethod_ = @Autowired)
    private ProcessLogsService processLogsService;
    @Setter(onMethod_ = @Autowired)
    private MetricsService metricsService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    private ObservableMap<Project, Set<ProcessWrapper>> processes;
    private SimpleIntegerProperty runningProjectsCount;


    private ProcessWrapper currentProcess = null;
    private TerminatedProcessInfo selectedTerminatedInfo = null;
    private final Map<ProcessWrapper, LogsAndMetricsTextComponent> logsAndMetricsTextComponents = new HashMap<>();
    private final Map<ProcessWrapper, MetricsChartComponent> metricsComponents = new HashMap<>();
    private StringProperty projectChoice;
    @Setter(onMethod_ = @Autowired)
    private PermissionRegistry permissionRegistry;

    /**
     * Will be called by InitializeAnnotationProcessor
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
        runningProjectsCount = new SimpleIntegerProperty(processes.size());
        runningProjectsCount.addListener((_, oldValue, newValue) -> Platform.runLater(() -> runningProjects.textProperty().set(localizationProvider.running_projects_count().get().replaceAll("\\d+", String.valueOf(newValue)))));
        localizationProvider.running_projects_count().addListener((_, oldValue, newValue) -> Platform.runLater(() -> runningProjects.textProperty().set(newValue.replaceAll("\\d+", String.valueOf(runningProjectsCount.getValue())))));
        runningProjects.textProperty().bindBidirectional(localizationProvider.running_projects_count());
        processes.addListener((MapChangeListener<? super Project, ? super Set<ProcessWrapper>>) change -> {
            if (change.wasAdded()) {
                Project project = change.getKey();

                CustomMenuItem menuItem = new CustomMenuItem(new Label(project.getTitle()));
                menuItem.setId(project.getTitle());
                menuItem.setHideOnClick(false);

                Set<ProcessWrapper> processWrappers = processes.get(change.getKey());

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
                            setupProcessMenuItem(item, project, processWrapper.getName(), processWrapper);
                            subMenu.getItems().add(item);
                        }

                        Bounds bounds = menuItem.getStyleableNode().localToScreen(menuItem.getStyleableNode().getBoundsInLocal());
                        subMenu.show(menuItem.getParentPopup(), bounds.getMaxX(), bounds.getMinY());
                    });
                }
                contextMenu.getItems().add(menuItem);
            } else if (change.wasRemoved()) {
                Platform.runLater(() -> contextMenu.getItems().removeIf(item -> item.getId() != null && item.getId().equals(change.getKey().getTitle())));
            }
            runningProjectsCount.set(processes.size());
        });
    }

    public void showRunningProjects() {
        refreshContextMenu();
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
        String label = "label";
        if (event.getSource() == logsButton) {
            logsButton.getStyleClass().clear();
            logsButton.getStyleClass().addAll(label, "logs-button-selected");
            metricsButton.getStyleClass().clear();
            metricsButton.getStyleClass().addAll(label, "logs-button");
            isLogsOpened = true;
        } else if (event.getSource() == metricsButton) {
            metricsButton.getStyleClass().clear();
            metricsButton.getStyleClass().addAll(label, "logs-button-selected");
            logsButton.getStyleClass().clear();
            logsButton.getStyleClass().addAll(label, "logs-button");
            isLogsOpened = false;
        }
        changeLogs(currentProcess, selectedTerminatedInfo);
    }

    private void setupProcessMenuItem(@NonNull CustomMenuItem menuItem, Project project, String text, ProcessWrapper processWrapper) {
        menuItem.setContent(new Label(text));
        menuItem.setHideOnClick(false);
        menuItem.setOnAction(_ -> Platform.runLater(() -> {
            projectChoice.unbind();
            projectChoice.set(project.getTitle() + " - " + processWrapper.getName());
            currentProcess = processWrapper;
            selectedTerminatedInfo = null;
            changeLogs(currentProcess, null);
        }));
    }

    private void refreshContextMenu() {
        contextMenu.getItems().clear();
        processes.forEach((project, processWrappers) -> {
            for (ProcessWrapper processWrapper : processWrappers) {
                ProjectRunEntry entry = new ProjectRunEntry(project, project.getTitle() + " - " + processWrapper.getName() + " " + localizationProvider.label_running().get(), processWrapper);

                CustomMenuItem item = new CustomMenuItem(new Label(entry.toString()));
                item.setOnAction(e -> Platform.runLater(() -> selectProjectRun(entry)));
                contextMenu.getItems().add(item);
            }
        });
        contextMenu.getItems().add(new SeparatorMenuItem());
        for (TerminatedProcessInfo info : processService.getRecentTerminatedRuns()) {
            ProjectRunEntry entry = new ProjectRunEntry(info);
            CustomMenuItem item = new CustomMenuItem(new Label(entry.toString()));
            item.setOnAction(e -> Platform.runLater(() -> selectProjectRun(entry)));
            contextMenu.getItems().add(item);
        }
        if (contextMenu.getItems().isEmpty()) {
            CustomMenuItem empty = new CustomMenuItem(new Label(localizationProvider.no_projects_available().get()));
            empty.setDisable(true);
            contextMenu.getItems().add(empty);
        }
    }

    private void selectProjectRun(ProjectRunEntry entry) {
        chosenProject.setText(entry.toString());

        if (entry.isRunning) {
            currentProcess = entry.runningProcess;
            selectedTerminatedInfo = null;
        } else {
            currentProcess = null;
            selectedTerminatedInfo = entry.terminatedInfo;
        }
        changeLogs(currentProcess, selectedTerminatedInfo);
    }

    /**
     * Universal method for showing logs or metrics for either a running or terminated process.
     */
    private void changeLogs(ProcessWrapper processWrapper, TerminatedProcessInfo terminatedInfo) {
        placeForScrollPane.getChildren().clear();

        if (isLogsOpened) {
            if (processWrapper != null) {
                // Live logs for running process
                var processLogs = processLogsService.getLogs().getOrDefault(processWrapper, new ProcessLogs());
                var flux = processLogsService.subscribeToLogs(processWrapper);
                var component = logsAndMetricsTextComponents.computeIfAbsent(processWrapper,
                        _ -> {
                            var componentProvider = new LogsAndMetricsTextComponent(processLogs, flux);
                            componentProvider.init();
                            return componentProvider;
                        });
                placeForScrollPane.getChildren().add(component.getComponent());
            } else if (terminatedInfo != null) {
                // Logs for terminated process
                var logs = terminatedInfo.logs();
                var component = new LogsAndMetricsTextComponent(logs, null);
                component.init();
                placeForScrollPane.getChildren().add(component.getComponent());
            }
        } else {
            if (processWrapper != null) {
                // Live metrics for running process
                var component = metricsComponents.computeIfAbsent(processWrapper,
                        _ -> new MetricsChartComponent(metricsService, processWrapper));
                placeForScrollPane.getChildren().add(component);
            } else if (terminatedInfo != null) {
                // Metrics for terminated process
                var metrics = terminatedInfo.metrics();
                var component = new MetricsChartComponent(metrics);
                placeForScrollPane.getChildren().add(component);
            }
        }
    }

    //helper class for dropdown entries
    public static class ProjectRunEntry {
        public final Project project;
        public final String displayName;
        public final boolean isRunning;
        public final ProcessWrapper runningProcess;
        public final TerminatedProcessInfo terminatedInfo;

        // For running process
        public ProjectRunEntry(Project project, String displayName, ProcessWrapper runningProcess) {
            this.project = project;
            this.displayName = displayName;
            this.isRunning = true;
            this.runningProcess = runningProcess;
            this.terminatedInfo = null;
        }
        // For terminated process
        public ProjectRunEntry(TerminatedProcessInfo info) {
            this.project = info.project();
            this.displayName = info.project().getTitle() + " - " + info.runConfig().getConfigName();
            this.isRunning = false;
            this.runningProcess = null;
            this.terminatedInfo = info;
        }

        @Override
        public String toString() { return displayName; }
    }

}
