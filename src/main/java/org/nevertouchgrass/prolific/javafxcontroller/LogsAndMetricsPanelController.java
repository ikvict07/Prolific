package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.components.LogsAndMetricsTextComponent;
import org.nevertouchgrass.prolific.components.MetricsChartComponent;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.nevertouchgrass.prolific.util.UIUtil.switchPaneChildren;

@Lazy
@Slf4j
@Component
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

    private boolean isLogsOpened = true;

    private final ContextMenu contextMenu = new ContextMenu();
    @Setter(onMethod_ = @Autowired)
    private ProcessService processService;
    @Setter(onMethod_ = @Autowired)
    private ProcessLogsService processLogsService;
    @Setter(onMethod_ = @Autowired)
    private MetricsService metricsService;
    private LocalizationProvider localizationProvider;
    private ObservableMap<Project, Set<ProcessWrapper>> processes;
    private SimpleIntegerProperty runningProjectsCount;

    private final Map<ProcessWrapper, LogsAndMetricsTextComponent> logsAndMetricsTextComponents = new HashMap<>();
    private ProcessWrapper currentProcess;
    private final SimpleStringProperty projectChoice = new SimpleStringProperty();


    @Autowired
    public void setLocalizationProvider(LocalizationProvider localizationProvider) {
        this.localizationProvider = localizationProvider;
        this.projectChoice.bind(localizationProvider.empty_chosen_project());
    }

    @FXML
    public void initialize() {
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());
        chosenProject.textProperty().bind(projectChoice);
    }

    /**
     * Will be called by InitializeAnnotationProcessor
     * @see org.nevertouchgrass.prolific.listener.InitializeAnnotationProcessor
     */
    @Initialize
    @SuppressWarnings("unused")
    public void init() {
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
                Platform.runLater(() -> contextMenu.getItems().removeIf(item -> item.getId().equals(change.getKey().getTitle())));
            }
            runningProjectsCount.set(processes.size());
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
        if (isLogsOpened) {
            var component = logsAndMetricsTextComponents.computeIfAbsent(processWrapper,
                    _ -> {
                        var componentProvider = new LogsAndMetricsTextComponent(processLogs, flux);
                        componentProvider.init();
                        return componentProvider;
                    });
            placeForScrollPane.getChildren().add(component.getLogsScrollPane());
        } else {
            var component = metricsComponents.computeIfAbsent(processWrapper,
                    _ -> new MetricsChartComponent(metricsService, processWrapper));
            placeForScrollPane.getChildren().add(component);
        }
    }
}
