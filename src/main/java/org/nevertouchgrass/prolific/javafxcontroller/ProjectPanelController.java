package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Data;
import org.nevertouchgrass.prolific.annotation.ConstraintsIgnoreElementSize;
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
    private HBox projectIcon;
    @FXML
    private VBox config;
    @FXML
    private HBox projectInfo;
    @FXML
    private VBox run;
    @FXML
    private HBox projectPanel;
    @FXML
    private Text projectIconText;
    @FXML
    private Label projectTitleText;
    @FXML
    private AnchorPane gradientBox;

    private Stage primaryStage;
    private ColorService colorService;

    private AnchorPaneConstraintsService anchorPaneConstraintsService;

    public void init() {
        String iconColorStyle = generateRandomColorStyle();
        projectIcon.setStyle(iconColorStyle);

        String baseColor = extractPrimaryColor(iconColorStyle);
        gradientBox.setStyle(generateGradientBoxStyle(baseColor));
        anchorPaneConstraintsService.setStage(primaryStage);
        anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeRight(projectInfo, 0.40);

        anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeLeft(run, 0.20);
        anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeLeft(config, 0.22);
        Runnable block = () -> {
            var width = projectPanel.getWidth();
            projectInfo.setMaxWidth(width * 0.32);
            projectInfo.setMinWidth(width * 0.32);
        };
        primaryStage.widthProperty().addListener((_, _, _) -> block.run());
        projectPanel.widthProperty().addListener((_, _, _) -> block.run());
        block.run();
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


    private double calculatePadding(double percents, Number width) {
        return width.doubleValue() * percents;
    }

    private String generateRandomColorStyle() {
        String color1 = colorService.generateBrightPastelColor();
        String color2 = colorService.generateSimilarBrightPastelColor(color1);

        return String.format("-fx-background-color: linear-gradient(to bottom, %s 0%%, %s 100%%);", color1, color2);
    }

    @Autowired

    private void set(Stage primaryStage, ColorService colorService, AnchorPaneConstraintsService anchorPaneConstraintsService) {
        this.primaryStage = primaryStage;
        this.colorService = colorService;
        this.anchorPaneConstraintsService = anchorPaneConstraintsService;
    }
}
