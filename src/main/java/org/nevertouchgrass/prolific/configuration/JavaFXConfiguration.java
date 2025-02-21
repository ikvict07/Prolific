package org.nevertouchgrass.prolific.configuration;

import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;

@Configuration
public class JavaFXConfiguration {

    @Bean(destroyMethod = "")
    public Stage stage() {
        CompletableFuture<Stage> future = new CompletableFuture<>();
        Platform.runLater(() -> future.complete(new Stage()));
        return future.join();
    }
}
