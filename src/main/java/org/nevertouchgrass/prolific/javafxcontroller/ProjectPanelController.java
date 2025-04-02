package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectRunConfigs;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.*;
import org.nevertouchgrass.prolific.service.icons.ProjectTypeIconRegistry;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.runner.DefaultProjectRunner;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.nevertouchgrass.prolific.util.UIUtil.switchPaneChildren;

@Slf4j
@Component
@Scope("prototype")
@Data
public class ProjectPanelController {
    public static final String PROJECT_RUN_ERROR_MESSAGE = "Error while running project {}";
    @FXML
    public StackPane star;
    @FXML
    public HBox runContent;
    @FXML
    private HBox projectIcon;
    @FXML
    private StackPane config;
    @FXML
    private HBox projectInfo;
    @FXML
    private StackPane run;
    @FXML
    private AnchorPane projectPanel;
    @FXML
    private Label projectIconText;
    @FXML
    private Label projectTitleText;
    @FXML
    private HBox configurationButton;
    @FXML
    private Label configurationName;
    @FXML
    private HBox controlPanel;
    @FXML
    private StackPane configTypeIcon;

    private Project project;

    private Stage primaryStage;
    private ColorService colorService;

    private AnchorPaneConstraintsService anchorPaneConstraintsService;
    private ProjectsRepository projectsRepository;
    private ProjectTypeIconRegistry projectTypeIconRegistry;

    private ContextMenu projectSettingsPopup;
    private ProjectSettingDropdownController projectSettingsDropdownController;

    private RunConfigService runConfigService;

    private ContextMenu contextMenu;
    private ProjectRunConfigs projectRunConfigs;
    private FxmlProvider fxmlProvider;

    private DefaultProjectRunner projectRunner;
    private NotificationService notificationService;
    private ProcessService processService;

    private RunConfig chosenConfig = null;
    private ProcessWrapper currentProcess = null;
    private Property<Boolean> isProjectRunning = new SimpleBooleanProperty(false);
    private ProjectsService projectsService;

    public void init() {
        String iconColorStyle = colorService.generateRandomColorStyle();
        projectIcon.setStyle(iconColorStyle);

        String baseColor = colorService.extractPrimaryColor(iconColorStyle);
        projectInfo.setStyle(colorService.generateGradientBoxStyle(baseColor));
        projectInfo.prefWidthProperty().bind(projectPanel.widthProperty().multiply(0.8));
        configurationName.maxWidthProperty().bind(projectInfo.widthProperty().multiply(0.3));

        projectRunConfigs = runConfigService.getAllRunConfigs(project);


        contextMenu = new ContextMenu();
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());

        generateContextMenuItems(projectRunConfigs.getManuallyAddedConfigs(), "Your configurations");
        generateContextMenuItems(projectRunConfigs.getImportedConfigs(), "Imported configurations");
        isProjectRunning.addListener((_, _, newValue) -> Platform.runLater(() -> {
            if (newValue) {
                runContent.getChildren().clear();
                var stopButton = fxmlProvider.getIcon("bigStopButton");
                runContent.getChildren().add(stopButton);
                run.setOnMouseClicked((_ -> stopProject()));
            } else {
                runContent.getChildren().clear();
                var runButton = fxmlProvider.getIcon("runButton");
                runContent.getChildren().add(runButton);
                run.setOnMouseClicked((_ -> runProject()));
            }
        }));
    }

    public void setProject(Project project) {
        this.project = project;
        star.setVisible(project.getIsStarred());
        StackPane stackPane = (StackPane) star.lookup("StackPane");
        stackPane.onMouseClickedProperty().set(_ -> {
            project.setIsStarred(false);
            star.setVisible(false);
            projectsRepository.update(project);
        });
        Tooltip tooltip = new Tooltip(project.getPath());
        projectTitleText.setTooltip(tooltip);
    }


    public void showProjectSetting() {
        Bounds bounds = config.localToScreen(config.getBoundsInLocal());
        projectSettingsPopup.setX(bounds.getCenterX());
        projectSettingsPopup.setY(bounds.getMaxY());
        Stage stage = (Stage) projectPanel.getScene().getWindow();
        projectSettingsDropdownController.setProject(project, projectSettingsPopup);
        projectSettingsPopup.show(stage);
    }

    public void showProjectConfigurations() {
        if (contextMenu.isShowing()) {
            contextMenu.hide();
        } else {
            Bounds bounds = controlPanel.localToScreen(controlPanel.getBoundsInLocal());
            contextMenu.show(controlPanel, bounds.getMinX(), bounds.getMaxY());
        }
    }

    private void switchConfigurationButtonIcon() {
        String resource = "unfoldButton".equalsIgnoreCase(configurationButton.getChildren().getFirst().getId()) ? "/icons/fxml/fold_button.fxml" : "/icons/fxml/unfold_button.fxml";
        switchPaneChildren(configurationButton, resource);
    }


    private void generateContextMenuItems(@NonNull List<RunConfig> runConfigs, String label) {
        ObservableList<MenuItem> menuItems = contextMenu.getItems();
        if (label != null && !runConfigs.isEmpty()) {
            MenuItem menuItem = new MenuItem(label);
            menuItem.addEventFilter(ActionEvent.ANY, Event::consume);
            menuItem.getStyleClass().add("menu-item-disabled");
            menuItems.add(menuItem);
        }

        for (RunConfig runConfig : runConfigs) {
            if (chosenConfig == null) {
                configTypeIcon.getChildren().clear();
                configTypeIcon.getChildren().add(projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
                configurationName.setText(runConfig.getConfigName());
                chosenConfig = runConfig;
            }
            MenuItem menuItem = new MenuItem(runConfig.getConfigName(), projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
            menuItem.setOnAction(_ -> {
                configurationName.setText(runConfig.getConfigName());
                configTypeIcon.getChildren().clear();
                configTypeIcon.getChildren().addAll(projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
                chosenConfig = runConfig;
            });
            menuItems.add(menuItem);
        }
    }

    public void runProject() {
        if (chosenConfig == null) {
            notificationService.notifyInfo(new InfoNotification("No configuration selected"));
            return;
        }
        try {
            notificationService.notifyInfo(InfoNotification.of("Running project {}", project.getTitle()));
            new Thread(this::runProjectLambda).start();
        } catch (Exception e) {
            notificationService.notifyError(ErrorNotification.of(e, PROJECT_RUN_ERROR_MESSAGE, project.getTitle()));
            log.error(PROJECT_RUN_ERROR_MESSAGE, project.getTitle(), e);
        }
    }

    public void onProcessDeath(ProcessWrapper process) {
        if (currentProcess != null) {
            var myPid = currentProcess.getPid();
            var pid = process.getPid();
            if (myPid == pid) {
                stopProject();
            }
        }
    }

    public void stopProject() {
        if (currentProcess != null) {
            currentProcess.getProcess().destroy();
            currentProcess = null;
            notificationService.notifyInfo(InfoNotification.of("Project {} stopped", project.getTitle()));
            log.info("Project {} stopped", project.getTitle());
        }
        isProjectRunning.setValue(false);
    }


    @Autowired
    private void set(Stage primaryStage, ColorService colorService, AnchorPaneConstraintsService anchorPaneConstraintsService, ProjectsRepository projectsRepository, Pair<ProjectSettingDropdownController, ContextMenu> projectSettingsPopup, RunConfigService runConfigService, ProjectTypeIconRegistry projectTypeIconRegistry, NotificationService notificationService, DefaultProjectRunner projectRunner, FxmlProvider fxmlProvider, ProcessService processService) {
        this.primaryStage = primaryStage;
        this.colorService = colorService;
        this.anchorPaneConstraintsService = anchorPaneConstraintsService;
        this.projectsRepository = projectsRepository;
        this.projectSettingsPopup = projectSettingsPopup.getValue();
        this.projectSettingsDropdownController = projectSettingsPopup.getKey();
        this.runConfigService = runConfigService;
        this.projectTypeIconRegistry = projectTypeIconRegistry;
        this.notificationService = notificationService;
        this.projectRunner = projectRunner;
        this.fxmlProvider = fxmlProvider;
        this.processService = processService;
    }

    private void runProjectLambda() {
        try {
            currentProcess = processService.runProject(project, chosenConfig);
            processService.addProcess(project, currentProcess);
            processService.registerOnKillListener(this::onProcessDeath);
            isProjectRunning.setValue(true);
        } catch (Exception e) {
            notificationService.notifyError(ErrorNotification.of(e, PROJECT_RUN_ERROR_MESSAGE, project.getTitle()));
            log.error(PROJECT_RUN_ERROR_MESSAGE, project.getTitle(), e);
            throw new IllegalStateException(e);
        }
    }
}
