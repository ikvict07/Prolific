package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Data;
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

    public void init() {
        String iconColorStyle = generateRandomColorStyle();
        projectIcon.setStyle(iconColorStyle);

        String baseColor = extractPrimaryColor(iconColorStyle);
        gradientBox.setStyle(generateGradientBoxStyle(baseColor));

        primaryStage.widthProperty().addListener((_, _, _) -> {
            var width = projectPanel.getWidth();
            projectInfo.setMaxWidth(width * 0.32);
            projectInfo.setMinWidth(width * 0.32);
            AnchorPane.setLeftAnchor(projectInfo, calculatePadding(0.01, width));
            AnchorPane.setRightAnchor(projectInfo, width - calculatePadding(0.33, width));

            AnchorPane.setLeftAnchor(run, calculatePadding(0.33, width));
            AnchorPane.setLeftAnchor(config, calculatePadding(0.35, width));
        });
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

    private void set(Stage primaryStage, ColorService colorService) {
        this.primaryStage = primaryStage;
        this.colorService = colorService;
    }
}
