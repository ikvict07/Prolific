package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.stage.Stage;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "configsStage")
@Lazy
public class RunConfigScreenController {
    @Setter(onMethod_ = {@Qualifier("configsStage"), @Autowired})
    private Stage stage;
    @FXML private Parent footer;

    @Initialize
    public void init() {
        stage.maximizedProperty().addListener((_, _, newValue) -> {
           if (newValue) {
               footer.getStyleClass().add("non-rounded");
           } else {
               footer.getStyleClass().remove("non-rounded");
           }
        });
    }
}
