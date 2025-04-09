package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
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
import javafx.util.Duration;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectRunConfigs;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.*;
import org.nevertouchgrass.prolific.service.icons.ProjectTypeIconRegistry;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.service.runner.DefaultProjectRunner;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

import static org.nevertouchgrass.prolific.util.UIUtil.switchPaneChildren;

@Slf4j
@Component
@Scope("prototype")
@Data
public class ProjectPanelController {
    public String PROJECT_RUN_ERROR_MESSAGE;
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
    private DefaultProjectRunner projectRunner;
    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;
    @Setter(onMethod_ = @Autowired)
    private ProcessService processService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;

    private ContextMenu contextMenu;
    private ProjectRunConfigs projectRunConfigs;

    private RunConfig chosenConfig = null;
    private ProcessWrapper currentProcess = null;
    private Property<Boolean> isProjectRunning = new SimpleBooleanProperty(false);
    private ProjectsService projectsService;

    public void init() {
        PROJECT_RUN_ERROR_MESSAGE = localizationProvider.log_error_running_project().get();

        String iconColorStyle = colorService.generateRandomColorStyle(colorService.getSeedForProject(project));
        projectIcon.setStyle(iconColorStyle);

        String baseColor = colorService.extractPrimaryColor(iconColorStyle);
        projectInfo.setStyle(colorService.generateGradientBoxStyle(baseColor, colorService.getSeedForProject(project)));
        projectInfo.prefWidthProperty().bind(projectPanel.widthProperty().multiply(0.8));
        configurationName.maxWidthProperty().bind(projectInfo.widthProperty().multiply(0.3));

        projectRunConfigs = runConfigService.getAllRunConfigs(project);

        contextMenu = new ContextMenu();
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());

        generateContextMenuItems(projectRunConfigs.getManuallyAddedConfigs(), localizationProvider.custom_configurations());
        generateContextMenuItems(projectRunConfigs.getImportedConfigs(), localizationProvider.imported_configurations());
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
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setHideDelay(Duration.millis(0));
        projectTitleText.setTooltip(tooltip);
        init();
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


    private void generateContextMenuItems(@NonNull List<RunConfig> runConfigs, StringProperty label) {
        ObservableList<MenuItem> menuItems = contextMenu.getItems();
        if (label != null && !runConfigs.isEmpty()) {
            MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(label);
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
            notificationService.notifyInfo(new InfoNotification(localizationProvider.log_no_configuration_selected()));
            return;
        }
        try {
            notificationService.notifyInfo(InfoNotification.of(localizationProvider.log_info_running_project(), project.getTitle()));
            new Thread(this::runProjectLambda).start();
        } catch (Exception e) {
            notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_running_project(), project.getTitle()));
            String error = MessageFormat.format(PROJECT_RUN_ERROR_MESSAGE, project.getTitle(), e);
            log.error(error);
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
            String error = MessageFormat.format(PROJECT_RUN_ERROR_MESSAGE, project.getTitle(), e);
            log.error(error);
            throw new IllegalStateException(e);
        }
    }
}
