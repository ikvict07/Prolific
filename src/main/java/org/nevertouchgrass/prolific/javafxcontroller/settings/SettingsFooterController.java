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

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsFooterController {
    @FXML
    public Label cancelButton;
    @FXML
    public Label applyButton;
    @FXML
    public Label submitButton;
    @FXML
    public AnchorPane settingsFooter;
    @FXML
    public HBox content;
    @Setter
    private Runnable saveRunnable;
    @Setter(onMethod_ = @Autowired)
    private SpringFXConfigurationProperties properties;

    @Setter(onMethod_ = @Autowired)
    private SettingsHeaderController settingsHeaderController;

    public void cancel() {
        settingsHeaderController.handleClose();
    }

    public void apply() {
        saveRunnable.run();
    }

    public void submit() {
        saveRunnable.run();
        settingsHeaderController.handleClose();
    }

    @SuppressWarnings("unused")
    public void help() {
        String url = properties.getGuidesUrl() + "settings.md";
        Thread.ofVirtual().start(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI(url));
                }
            } catch (IOException | URISyntaxException _) {}
        });
    }


    public void changeApplyButtonStyle(boolean isDisabled) {
        if (isDisabled != applyButton.isDisabled()) {
            applyButton.getStyleClass().clear();
            if (applyButton.isDisabled()) {
                applyButton.getStyleClass().add("settings-submit-button");
            } else {
                applyButton.getStyleClass().add("settings-apply-button");
            }
            applyButton.setDisable(isDisabled);
        }
    }
}
