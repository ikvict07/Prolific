package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import lombok.Data;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.AnchorPaneConstraintsService;
import org.nevertouchgrass.prolific.service.ColorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

    private Project project;

    private Stage primaryStage;
    private ColorService colorService;

    private AnchorPaneConstraintsService anchorPaneConstraintsService;
    private ProjectsRepository projectsRepository;

    private Popup projectSettingsPopup;

    public void init() {
        String iconColorStyle = generateRandomColorStyle();
        projectIcon.setStyle(iconColorStyle);

        String baseColor = extractPrimaryColor(iconColorStyle);
        projectInfo.setStyle(generateGradientBoxStyle(baseColor));
        projectInfo.prefWidthProperty().bind(projectPanel.widthProperty().multiply(0.6));
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
    private void set(Stage primaryStage, ColorService colorService, AnchorPaneConstraintsService anchorPaneConstraintsService, ProjectsRepository projectsRepository, Popup projectSettingsPopup) {
        this.primaryStage = primaryStage;
        this.colorService = colorService;
        this.anchorPaneConstraintsService = anchorPaneConstraintsService;
        this.projectsRepository = projectsRepository;
        this.projectSettingsPopup = projectSettingsPopup;
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
}
