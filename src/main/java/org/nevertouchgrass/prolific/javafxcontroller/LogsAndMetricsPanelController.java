package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    private final SimpleStringProperty projectChoice = new SimpleStringProperty("None");

    @FXML
    public void initialize() {
        projectLogsDropdown.prefWidthProperty().bind(logsAndMetrics.widthProperty().multiply(0.3));
        chosenProject.textProperty().bind(projectChoice);
    }

    public void switchConfigurationButtonIcon() {
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
