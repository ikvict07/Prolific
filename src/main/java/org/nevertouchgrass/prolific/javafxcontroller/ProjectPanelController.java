package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.javafxcontroller.settings.RunConfigSettingHeaderController;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectRunConfigs;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.ColorService;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.ProjectsService;
import org.nevertouchgrass.prolific.service.configurations.RunConfigService;
import org.nevertouchgrass.prolific.service.icons.ProjectTypeIconRegistry;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.service.runner.ProjectRunnerRegistry;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.nevertouchgrass.prolific.util.UIUtil.switchPaneChildren;

@Slf4j
@StageComponent
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
    @Setter(onMethod_ = @Autowired)
    private Stage primaryStage;
    @Setter(onMethod_ = @Autowired)
    private ColorService colorService;
    @Setter(onMethod_ = @Autowired)
    private ProjectsRepository projectsRepository;
    @Setter(onMethod_ = @Autowired)
    private ProjectTypeIconRegistry projectTypeIconRegistry;
    @Setter(onMethod_ = @Autowired)
    private ContextMenu projectSettingsPopup;
    @Setter(onMethod_ = @Autowired)
    private ProjectSettingDropdownController projectSettingsDropdownController;
    @Setter(onMethod_ = @Autowired)
    private RunConfigService runConfigService;
    @Setter(onMethod_ = @Autowired)
    private NotificationService notificationService;
    @Setter(onMethod_ = @Autowired)
    private ProjectRunnerRegistry projectRunner;
    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;
    @Setter(onMethod_ = @Autowired)
    private ProcessService processService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    @Setter(onMethod_ = @Autowired)
    private RunConfigSettingHeaderController runConfigSettingHeaderController;

    private ContextMenu contextMenu;
    private ProjectRunConfigs projectRunConfigs;

    private RunConfig chosenConfig = null;
    private ProcessWrapper currentProcess = null;
    private Property<Boolean> isProjectRunning = new SimpleBooleanProperty(false);
    private ProjectsService projectsService;

    private final List<Consumer<Project>> updateListeners = new ArrayList<>();

    public void init() {
        String iconColorStyle = colorService.generateRandomColorStyle(colorService.getSeedForProject(project));
        projectIcon.setStyle(iconColorStyle);

        String baseColor = colorService.extractPrimaryColor(iconColorStyle);
        projectInfo.setStyle(colorService.generateGradientBoxStyle(baseColor, colorService.getSeedForProject(project)));
        projectInfo.prefWidthProperty().bind(projectPanel.widthProperty().multiply(0.8));
        configurationName.maxWidthProperty().bind(projectInfo.widthProperty().multiply(0.3));

        contextMenu = new ContextMenu();
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());

        initializeProjectConfiguration();
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

    private void onAddConfig() {
        runConfigSettingHeaderController.setProjectPanelController(this);
        runConfigSettingHeaderController.open();
    }

    private void generateAddRunConfigContextMenuItem(StringProperty label) {
        ObservableList<MenuItem> menuItems = contextMenu.getItems();
        if (!(projectRunConfigs.getImportedConfigs().isEmpty() && projectRunConfigs.getManuallyAddedConfigs().isEmpty())) {
            SeparatorMenuItem separator = new SeparatorMenuItem();
            menuItems.add(separator);
        }
        MenuItem menuItem = new MenuItem();
        menuItem.textProperty().bind(label);
        menuItem.setGraphic(fxmlProvider.getIcon("addButton"));
        menuItem.addEventFilter(ActionEvent.ANY, _ -> onAddConfig());
        menuItems.add(menuItem);
    }

    public void addUpdateListener(Consumer<Project> listener) {
        updateListeners.add(listener);
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
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(0));
        projectTitleText.setTooltip(tooltip);
        init();
    }

    public void updateStar() {
        star.setVisible(project.getIsStarred());
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
            contextMenu.show(controlPanel, Side.BOTTOM, 0, 4);
        }
    }

    public void initializeProjectConfiguration() {
        contextMenu.getItems().clear();
        projectRunConfigs = runConfigService.getAllRunConfigs(project);
        generateContextMenuItems(projectRunConfigs.getManuallyAddedConfigs(), localizationProvider.custom_configurations());
        generateContextMenuItems(projectRunConfigs.getImportedConfigs(), localizationProvider.imported_configurations());
        generateAddRunConfigContextMenuItem(localizationProvider.create_config());
    }

    private void switchConfigurationButtonIcon() {
        String resource = "unfoldButton".equalsIgnoreCase(configurationButton.getChildren().getFirst().getId()) ? "/icons/fxml/fold_button.fxml" : "/icons/fxml/unfold_button.fxml";
        switchPaneChildren(configurationButton, resource);
    }


    private void generateContextMenuItems(@NonNull List<RunConfig> runConfigs, StringProperty label) {
        ObservableList<MenuItem> menuItems = contextMenu.getItems();
        if (label != null && !runConfigs.isEmpty()) {
            MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(label);
            menuItem.setDisable(true);
            menuItems.add(menuItem);
        }

        for (RunConfig runConfig : runConfigs) {
            if (chosenConfig == null) {
                configTypeIcon.getChildren().clear();
                configTypeIcon.getChildren().add(projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
                configurationName.setText(runConfig.getConfigName());
                chosenConfig = runConfig;
            }
            MenuItem menuItem;
            if (label != null && label.equals(localizationProvider.custom_configurations())) {
                Label config = new Label(runConfig.getConfigName(), projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));

                Parent removeButton = fxmlProvider.getIcon("removeBin");

                removeButton.setOnMouseClicked(_ -> {
                    runConfigService.deleteRunConfig(project, runConfig);
                    if (chosenConfig.equals(runConfig)) {
                        chosenConfig = null;
                    }
                    initializeProjectConfiguration();
                });

                HBox content = new HBox();
                content.setAlignment(Pos.CENTER);
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                content.getChildren().addAll(config, spacer, removeButton);

                content.prefWidthProperty().bind(contextMenu.widthProperty().subtract(48));

                menuItem = new CustomMenuItem(content);
            } else {
                menuItem = new MenuItem(runConfig.getConfigName(), projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
            }
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
            notificationService.notifyInfo(new InfoNotification(localizationProvider.log_no_configuration_selected()));
            return;
        }
        try {
            notificationService.notifyInfo(InfoNotification.of(localizationProvider.log_info_running_project(), project.getTitle()));
            new Thread(this::runProjectLambda).start();
        } catch (Exception e) {
            notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_running_project(), project.getTitle()));
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
            currentProcess.getProcess().onExit().thenAccept(_ -> new Thread(() -> updateListeners.forEach(c -> c.accept(project))).start());
            currentProcess.getProcess().destroy();
            currentProcess = null;
            notificationService.notifyInfo(InfoNotification.of(localizationProvider.log_info_project_stopped(), project.getTitle()));
            log.info("Project {} stopped", project.getTitle());
        }
        isProjectRunning.setValue(false);

    }

    private void runProjectLambda() {
        try {
            currentProcess = processService.runProject(project, chosenConfig);
            processService.addProcess(project, currentProcess);
            processService.registerOnKillListener(this::onProcessDeath);
            isProjectRunning.setValue(true);
        } catch (Exception e) {
            notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_running_project(), project.getTitle()));
            log.error(PROJECT_RUN_ERROR_MESSAGE, project.getTitle(), e);
            throw new IllegalStateException(e);
        }
        updateListeners.forEach(c -> c.accept(project));
    }
}
