package org.nevertouchgrass.prolific.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class UIUtil {

    public static void switchPaneChildren(@NonNull Pane pane, @NonNull String resource) {
        Platform.runLater(() -> {
            try {
                pane.getChildren().clear();
                Pane parent = new FXMLLoader(UIUtil.class.getResource(resource)).load();
                Node substitute = parent.getChildren().getFirst();
                pane.getChildren().add(substitute);
            } catch (IOException e) {
                log.error("Error occurred while trying to load the resource: {} {}", resource, e.getMessage());
            }
        });
    }
}
