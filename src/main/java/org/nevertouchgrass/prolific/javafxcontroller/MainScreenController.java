package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

@Lazy
@Slf4j
@StageComponent
@SuppressWarnings("unused")
public class MainScreenController {
    @Setter(onMethod_ = {@Qualifier("primaryStage"), @Autowired})
    private Stage stage;
    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;

    @FXML private AnchorPane mainScreen;
    @FXML private VBox logsAndMetricsPanel;
    @FXML private AnchorPane footer;
    @FXML private VBox projectsPanel;
    @FXML private StackPane header;

    @FXML private AnchorPane headerMac;

    public void initialize() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            header.getChildren().add(fxmlProvider.getFxmlResource("headerMac").getParent());
        } else {
            header.getChildren().add(fxmlProvider.getFxmlResource("headerCommon").getParent());
        }
    }

    @Initialize
    public void init() {
        ChangeListener<Number> block1 = (observable, oldValue, newValue) -> mainScreen.setPrefWidth(newValue.doubleValue());
        ChangeListener<Number> block2 = (observable, oldValue, newValue) -> mainScreen.setPrefHeight(newValue.doubleValue());
        stage.widthProperty().addListener(block1);
        stage.heightProperty().addListener(block2);
        stage.maximizedProperty().addListener((_, _, newValue) -> {
           if (newValue) {
               footer.getStyleClass().add("non-rounded");
           } else {
               footer.getStyleClass().remove("non-rounded");
           }
        });
    }
}
