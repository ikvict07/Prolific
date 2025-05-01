package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class RunConfigScreenController {
    @FXML private AnchorPane footer;
    @FXML private StackPane configsHeader;

    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;

    public void initialize() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            configsHeader.getChildren().add(fxmlProvider.getFxmlResource("configsHeaderMac").getParent());
        } else {
            configsHeader.getChildren().add(fxmlProvider.getFxmlResource("configsHeaderCommon").getParent());
        }
    }
}
