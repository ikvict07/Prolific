package org.nevertouchgrass.prolific.components;

import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Objects;

@Configuration
public class AlertWindowConfiguration {

    @Bean
    @Scope("prototype")
    public Alert alert(FxmlProvider fxmlProvider) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
        alert.getDialogPane().setStyle("-fx-background-radius: 0;");
        StackPane svgGraphic = (StackPane) fxmlProvider.getFxmlResource("error").getParent();
        svgGraphic.setPrefSize(28, 28);
        alert.getDialogPane().setGraphic(svgGraphic);
        return alert;
    }

}
