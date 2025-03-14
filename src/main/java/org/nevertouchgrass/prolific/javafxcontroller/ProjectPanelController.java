package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectRunConfigs;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.AnchorPaneConstraintsService;
import org.nevertouchgrass.prolific.service.ColorService;
import org.nevertouchgrass.prolific.service.RunConfigService;
import org.nevertouchgrass.prolific.service.icons.ProjectTypeIconRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Scope("prototype")
@Data
public class ProjectPanelController {
    @FXML
    public StackPane star;
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

    private Popup projectSettingsPopup;

    private RunConfigService runConfigService;

    private ContextMenu contextMenu;
    private ProjectRunConfigs projectRunConfigs;

    public void init() {
        String iconColorStyle = generateRandomColorStyle();
        projectIcon.setStyle(iconColorStyle);

        String baseColor = extractPrimaryColor(iconColorStyle);
        projectInfo.setStyle(generateGradientBoxStyle(baseColor));
        projectInfo.prefWidthProperty().bind(projectPanel.widthProperty().multiply(0.8));
        configurationName.maxWidthProperty().bind(projectInfo.widthProperty().multiply(0.3));

        projectRunConfigs = runConfigService.getAllRunConfigs(project);


        contextMenu = new ContextMenu();
        contextMenu.showingProperty().addListener((_, _, _) -> switchConfigurationButtonIcon());

        generateContextMenuItems(projectRunConfigs.getManuallyAddedConfigs(), "Your configurations");
        generateContextMenuItems(projectRunConfigs.getImportedConfigs(), "Imported configurations");

        if (contextMenu.getItems().isEmpty()) {
            configurationName.setText("");
            configTypeIcon.getChildren().clear();
            configTypeIcon.getChildren().add(projectTypeIconRegistry.getConfigTypeIcon(""));
        }
    }


    private String generateGradientBoxStyle(String baseColor) {
        String highlightColor = colorService.generateSimilarBrightPastelColor(baseColor);

        return String.format(
                "-fx-background-color: linear-gradient(from 0%% 0%% to 100%% 0%%, transparent 0%%, %4s99 30%%, transparent 100%%);",
                highlightColor);
    }

    private String extractPrimaryColor(String style) {
        int startIndex = style.indexOf("#");
        int endIndex = style.indexOf(" ", startIndex);
        return style.substring(startIndex, endIndex);
    }

    private String generateRandomColorStyle() {
        String color1 = colorService.generateBrightPastelColor();
        String color2 = colorService.generateSimilarBrightPastelColor(color1);

        return String.format("-fx-background-color: linear-gradient(to bottom, %s 0%%, %s 100%%);", color1, color2);
    }

    @Autowired
    private void set(Stage primaryStage, ColorService colorService, AnchorPaneConstraintsService anchorPaneConstraintsService, ProjectsRepository projectsRepository, Popup projectSettingsPopup, RunConfigService runConfigService, ProjectTypeIconRegistry projectTypeIconRegistry) {
        this.primaryStage = primaryStage;
        this.colorService = colorService;
        this.anchorPaneConstraintsService = anchorPaneConstraintsService;
        this.projectsRepository = projectsRepository;
        this.projectSettingsPopup = projectSettingsPopup;
        this.runConfigService = runConfigService;
        this.projectTypeIconRegistry = projectTypeIconRegistry;
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
    }


    public void showProjectSetting() {
        Bounds bounds = config.localToScreen(config.getBoundsInLocal());
        projectSettingsPopup.setX(bounds.getCenterX());
        projectSettingsPopup.setY(bounds.getMaxY());
        Stage stage = (Stage) projectPanel.getScene().getWindow();
        Parent projectSettingDropdownParent = (Parent) projectSettingsPopup.getProperties().get("content");
        ProjectSettingDropdownController controller = (ProjectSettingDropdownController) projectSettingDropdownParent.getProperties().get("controller");
        controller.setProject(project);
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
        try {
            HBox substituteIcon = "unfoldButton".equals(configurationButton.getChildren().getFirst().getId()) ?
                    new FXMLLoader(getClass().getResource("/icons/fxml/fold_button.fxml")).load() :
                    new FXMLLoader(getClass().getResource("/icons/fxml/unfold_button.fxml")).load();
            configurationButton.getChildren().clear();
            configurationButton.getChildren().add(substituteIcon.getChildren().getFirst());
        } catch (IOException e) {
            log.error("Error retrieving fxml resource: {}", e.getMessage());
        }
    }


    private void generateContextMenuItems(@NonNull List<RunConfig> runConfigs, String label) {
        ObservableList<MenuItem> menuItems = contextMenu.getItems();
        if (label != null && !runConfigs.isEmpty()) {
            MenuItem menuItem = new MenuItem(label);
            menuItem.setDisable(true);
            menuItems.add(menuItem);
        }

        for (RunConfig runConfig : runConfigs) {
            if (configTypeIcon.getChildren().isEmpty()) {
                configTypeIcon.getChildren().add(projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
                configurationName.setText(runConfig.getConfigName());
            }
            MenuItem menuItem = new MenuItem(runConfig.getConfigName(), projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
            menuItem.setOnAction( _ -> {
                configurationName.setText(runConfig.getConfigName());
                configTypeIcon.getChildren().clear();
                configTypeIcon.getChildren().addAll(projectTypeIconRegistry.getConfigTypeIcon(runConfig.getType()));
            });
            menuItems.add(menuItem);
        }
    }
}
