package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.model.LogWrapper;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.ProcessService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@SuppressWarnings("java:S1450")
public class LogsAndMetricsPanelController {
    @FXML
    private TextArea logsAndMetrics;
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

    private final ContextMenu contextMenu = new ContextMenu();

    private ProcessService processService;
    private ProcessLogsService processLogsService;

    private ObservableMap<Project, Set<ProcessWrapper>> processes;
    private SimpleIntegerProperty runningProjectsCount;

    private final CopyOnWriteArrayList<ProcessLogs> processLogsList = new CopyOnWriteArrayList<>();

    @Autowired
    public void set(ProcessService processService, ProcessLogsService processLogsService) {
        this.processService = processService;
        this.processLogsService = processLogsService;
    }

    private final SimpleStringProperty projectChoice = new SimpleStringProperty("None");

    @FXML
    public void initialize() {
        projectLogsDropdown.prefWidthProperty().bind(logsAndMetrics.widthProperty().multiply(0.3));
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());
        chosenProject.textProperty().bind(projectChoice);
        logsAndMetrics.textProperty().addListener((_, _, _) -> logsAndMetrics.selectEnd());
    }

    @Initialize
    @SuppressWarnings("unused") // Will be called by BPP
    public void init() {
        processes = processService.getObservableLiveProcesses();
        runningProjectsCount = new SimpleIntegerProperty(processes.size());
        runningProjectsCount.addListener((_, _, newValue) -> Platform.runLater(() -> runningProjects.setText(runningProjects.getText().replaceAll("\\d+", String.valueOf(newValue)))));

        processes.addListener((MapChangeListener<? super Project, ? super Set<ProcessWrapper>>) change -> {
            if (change.wasAdded()) {
                CustomMenuItem menuItem = new CustomMenuItem(new Label(change.getKey().getTitle()));
                menuItem.setId(change.getKey().getTitle());
                menuItem.setHideOnClick(false);

                Set<ProcessWrapper> processWrappers = processes.get(change.getKey());

                if (processWrappers.size() == 1) {
                    setupProcessMenuItem(menuItem, change.getKey().getTitle(), processWrappers.iterator().next());
                    menuItem.addEventHandler(ActionEvent.ACTION, _ -> projectChoice.set(change.getKey().getTitle()));
                } else {
                    menuItem.addEventHandler(ActionEvent.ACTION, _ -> {
                        projectChoice.set(change.getKey().getTitle());

                        ContextMenu subMenu = new ContextMenu();

                        if (processWrappers.size() == 1) {
                            setupProcessMenuItem(menuItem, change.getKey().getTitle(), processWrappers.iterator().next());
                        } else {
                            for (ProcessWrapper processWrapper : processWrappers) {
                                CustomMenuItem item = new CustomMenuItem();
                                setupProcessMenuItem(item, processWrapper.getName(), processWrapper);
                                subMenu.getItems().add(item);
                            }
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
        try {
            HBox substituteIcon = "unfoldButton".equals(foldButton.getChildren().getFirst().getId()) ?
                    new FXMLLoader(getClass().getResource("/icons/fxml/fold_button.fxml")).load() :
                    new FXMLLoader(getClass().getResource("/icons/fxml/unfold_button.fxml")).load();
            foldButton.getChildren().clear();
            foldButton.getChildren().add(substituteIcon.getChildren().getFirst());
        } catch (IOException e) {
            log.error("Error retrieving fxml resource: {}", e.getMessage());
        }
    }

    public void switchLogsButtonStyle(MouseEvent event) {
        String label = "label";
        if (event.getSource() == logsButton) {
            logsButton.getStyleClass().clear();
            logsButton.getStyleClass().addAll(label, "logs-button-selected");
            metricsButton.getStyleClass().clear();
            metricsButton.getStyleClass().addAll(label, "logs-button");
        } else if (event.getSource() == metricsButton) {
            metricsButton.getStyleClass().clear();
            metricsButton.getStyleClass().addAll(label, "logs-button-selected");
            logsButton.getStyleClass().clear();
            logsButton.getStyleClass().addAll(label, "logs-button");
        }
    }

    private ProcessWrapper selectedProcessWrapper = null;
    private final Map<ProcessWrapper, Disposable> subscriptions = new HashMap<>();

    private void setupProcessMenuItem(@NonNull CustomMenuItem menuItem, String text, ProcessWrapper processWrapper) {
        menuItem.setContent(new Label(text));
        menuItem.setHideOnClick(false);

        menuItem.setOnAction(_ -> {
            selectedProcessWrapper = processWrapper;

            ProcessLogs processLogs = processLogsService.getLogs().getOrDefault(processWrapper, new ProcessLogs());
            Platform.runLater(() -> {
                logsAndMetrics.clear();
                logsAndMetrics.setText("\n" + String.join("\n", processLogs.getLogs().stream().map(LogWrapper::getLog).toList()));
            });
            if (!subscriptions.containsKey(processWrapper)) {
                var logsFlux = processLogsService.subscribeToLogs(processWrapper);
                var subscription = logsFlux.subscribe(l -> Platform.runLater(() -> {
                    if (!selectedProcessWrapper.equals(processWrapper)) {
                        return;
                    }
                    logsAndMetrics.appendText("\n" + l.getLog());
                }));
                subscriptions.put(processWrapper, subscription);
            }

        });
    }
}
