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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.ProcessService;
import org.nevertouchgrass.prolific.util.OSProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
@Component
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

    private ObservableMap<Project, Set<OSProcessWrapper>> processes;
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
    }

    @Initialize
    public void init() {
        processes = processService.getObservableLiveProcesses();
        runningProjectsCount = new SimpleIntegerProperty(processes.size());
        runningProjectsCount.addListener((_, _, newValue) -> Platform.runLater(() -> runningProjects.setText(runningProjects.getText().replaceAll("\\d+", String.valueOf(newValue)))));

        processes.addListener((MapChangeListener<? super Project, ? super Set<OSProcessWrapper>>) change -> {
            if (change.wasAdded()) {
                CustomMenuItem menuItem = new CustomMenuItem(new Label(change.getKey().getTitle()));
                menuItem.setId(change.getKey().getTitle());
                menuItem.setHideOnClick(false);

                menuItem.addEventHandler(ActionEvent.ACTION,  _ -> {
                    projectChoice.set(change.getKey().getTitle());

                    ContextMenu subMenu = new ContextMenu();

                    for (OSProcessWrapper osProcessWrapper : processes.get(change.getKey())) {
                        MenuItem item = new MenuItem(osProcessWrapper.getProcess().getName());

                        item.setOnAction(_ -> {
                            ProcessLogs processLogs = processLogsService.getLogs().getOrDefault(osProcessWrapper, new ProcessLogs());
                            Consumer<String> logAddedListener = _ -> {
                                Platform.runLater(() -> {
                                    List<String> logs = processLogs.getLogs();
                                    logsAndMetrics.setText(String.join("\n", logs));
                                    logsAndMetrics.positionCaret(logsAndMetrics.getLength() - logs.getLast().length());
                                });
                            };

                            logsAndMetrics.clear();
                            processLogsList.forEach(ProcessLogs::clearOnLogAddedListeners);
                            processLogsList.clear();
                            processLogsList.add(processLogs);

                            processLogs.addOnLogAddedListener(logAddedListener);
                            if (processLogs.getLogs().stream().mapToInt(String::length).sum() > logsAndMetrics.getLength()) {
                                logAddedListener.accept(null);
                            }
                        });
                        subMenu.getItems().add(item);
                    }
                    Bounds bounds = menuItem.getStyleableNode().localToScreen(menuItem.getStyleableNode().getBoundsInLocal());
                    subMenu.show(menuItem.getParentPopup(), bounds.getMaxX(), bounds.getMinY());
                });
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
        if (event.getSource() == logsButton) {
            logsButton.getStyleClass().clear();
            logsButton.getStyleClass().addAll("label", "logs-button-selected");
            metricsButton.getStyleClass().clear();
            metricsButton.getStyleClass().addAll("label","logs-button");
        } else if (event.getSource() == metricsButton) {
            metricsButton.getStyleClass().clear();
            metricsButton.getStyleClass().addAll("label", "logs-button-selected");
            logsButton.getStyleClass().clear();
            logsButton.getStyleClass().addAll("label", "logs-button");
        }
    }
}
