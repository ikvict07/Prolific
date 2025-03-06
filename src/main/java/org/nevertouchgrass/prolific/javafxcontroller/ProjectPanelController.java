package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;
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
    public VBox star;
    @FXML
    private HBox projectIcon;
    @FXML
    private VBox config;
    @FXML
    private HBox projectInfo;
    @FXML
    private VBox run;
    @FXML
    private AnchorPane projectPanel;
    @FXML
    private Text projectIconText;
    @FXML
    private Label projectTitleText;

    private Project project;

    private Stage primaryStage;
    private ColorService colorService;

    private AnchorPaneConstraintsService anchorPaneConstraintsService;
    private ProjectsRepository projectsRepository;

    public void init() {
        String iconColorStyle = generateRandomColorStyle();
        projectIcon.setStyle(iconColorStyle);

        String baseColor = extractPrimaryColor(iconColorStyle);
        projectPanel.setStyle(generateGradientBoxStyle(baseColor));
        anchorPaneConstraintsService.setStage(primaryStage);
        anchorPaneConstraintsService.setElementWidth(projectInfo, 0.30);
        anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeRight(projectInfo, 0.30);
        anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeRight(star, 0.15);

        anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeLeft(run, 0.20);
        anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeLeft(config, 0.22);

        var starImage = (SVGPath) star.lookup("SVGPath");
        starImage.hoverProperty().addListener((_, _, newVal) -> {
            if (star != null) {
                if (newVal) {
                    starImage.setFillRule(FillRule.EVEN_ODD);
                    starImage.setFill(Color.valueOf("#9e9e9e"));
                } else {
                    starImage.setFillRule(FillRule.NON_ZERO);
                    starImage.setFill(Color.valueOf("#F2C55C"));
                }
            }
        });

    }


    public void setStarred(boolean isStarred) {
        star.setVisible(isStarred);
    }

    private String generateGradientBoxStyle(String baseColor) {
        String highlightColor = colorService.generateSimilarBrightPastelColor(baseColor);

        return String.format(
                "-fx-background-color: linear-gradient(from 0%% 0%% to 100%% 0%%, #2B2D30 0%%, %s 16.5%%, #2B2D30 33%%, #2B2D30 100%%);",
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
    private void set(Stage primaryStage, ColorService colorService, AnchorPaneConstraintsService anchorPaneConstraintsService, ProjectsRepository projectsRepository) {
        this.primaryStage = primaryStage;
        this.colorService = colorService;
        this.anchorPaneConstraintsService = anchorPaneConstraintsService;
        this.projectsRepository = projectsRepository;
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
}
