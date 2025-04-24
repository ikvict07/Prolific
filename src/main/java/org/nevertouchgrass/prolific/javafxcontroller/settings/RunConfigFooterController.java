package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@StageComponent(stage = "configsStage")
@Lazy
@SuppressWarnings("unused")
public class RunConfigFooterController {

    @Setter(onMethod_ = @Autowired)
    private RunConfigSettingHeaderController runConfigSettingHeaderController;

    @FXML private AnchorPane configFooter;
    @FXML private HBox content;

    @FXML private Label cancelButton;
    @FXML private Label submitButton;

    @Setter
    private Runnable saveRunnable;
    @Setter(onMethod_ = @Autowired)
    private SpringFXConfigurationProperties properties;

    @FXML
    private void submit() {
        saveRunnable.run();
    }

    public void help() {
        String url = properties.getGuidesUrl() + "configs.md";
        Thread.ofVirtual().start(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (IOException | URISyntaxException _) {}
        });
    }

    @FXML
    private void cancel() {
        runConfigSettingHeaderController.handleClose();
    }

    public void changeApplyButtonStyle(boolean isDisabled) {
        if (isDisabled != submitButton.isDisabled()) {
            submitButton.getStyleClass().clear();
            if (submitButton.isDisabled()) {
                submitButton.getStyleClass().add("settings-submit-button");
            } else {
                submitButton.getStyleClass().add("settings-apply-button");
            }
            submitButton.setDisable(isDisabled);
        }
    }
}
