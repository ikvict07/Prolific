package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsScreenController {
    @FXML
    public BorderPane settingsScreen;
    @FXML
    public StackPane settingsHeader;
    @FXML
    public SplitPane settingsList;
    @FXML
    public AnchorPane settingsFooter;

    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;


    public void initialize() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            settingsHeader.getChildren().add(fxmlProvider.getFxmlResource("settingsHeaderMac").getParent());
        } else {
            settingsHeader.getChildren().add(fxmlProvider.getFxmlResource("settingsHeaderCommon").getParent());
        }
    }
}
