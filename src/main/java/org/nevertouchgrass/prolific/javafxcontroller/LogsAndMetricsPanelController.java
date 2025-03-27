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
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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

import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static org.nevertouchgrass.prolific.util.UIUtil.switchPaneChildren;

@Slf4j
@Component
@SuppressWarnings("java:S1450")
public class LogsAndMetricsPanelController {
    @FXML
    private TextFlow logsAndMetrics;
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
    private ScrollPane scrollPane;

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
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());
        chosenProject.textProperty().bind(projectChoice);
        logsAndMetrics.heightProperty().addListener((_, _, _) -> scrollPane.setVvalue(1));
    }

    @Initialize
    public void init() {
        processes = processService.getObservableLiveProcesses();
        runningProjectsCount = new SimpleIntegerProperty(processes.size());
        runningProjectsCount.addListener((_, _, newValue) -> Platform.runLater(() -> runningProjects.setText(runningProjects.getText().replaceAll("\\d+", String.valueOf(newValue)))));

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
            metricsButton.getStyleClass().addAll(label,"logs-button");
        } else if (event.getSource() == metricsButton) {
            metricsButton.getStyleClass().clear();
            metricsButton.getStyleClass().addAll(label, "logs-button-selected");
            logsButton.getStyleClass().clear();
            logsButton.getStyleClass().addAll(label, "logs-button");
        }
    }

    private void setupProcessMenuItem(@NonNull CustomMenuItem menuItem, Project project, String text, ProcessWrapper processWrapper) {
        menuItem.setContent(new Label(text));
        menuItem.setHideOnClick(false);

        menuItem.setOnAction(_ -> {
            ProcessLogs processLogs = processLogsService.getLogs().getOrDefault(processWrapper, new ProcessLogs());
            Consumer<LogWrapper> logAddedListener = _ -> Platform.runLater(() -> {
                projectChoice.set(project.getTitle() + " - " + processWrapper.getName());
                logsAndMetrics.getChildren().clear();
                Queue<LogWrapper> logs = processLogs.getLogs();
                List<Text> newText = logs.stream().map(it -> {
                    Text itText = new Text(it.getLog() + "\n");
                    itText.getStyleClass().add("log-text");
                    return itText;
                }).toList();
                logsAndMetrics.getChildren().addAll(newText);
            });

            logsAndMetrics.getChildren();
            processLogsList.forEach(ProcessLogs::clearOnLogAddedListeners);
            processLogsList.clear();
            processLogsList.add(processLogs);

            processLogs.addOnLogAddedListener(logAddedListener);
            if (processLogs.getLogs().stream().mapToInt(it -> it.getLog().length()).sum() > logsAndMetrics.getChildren().stream().mapToInt(it -> ((Text)it).getText().length()).sum()) {
                logAddedListener.accept(null);
            }
        });
    }
}
