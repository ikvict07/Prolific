package org.nevertouchgrass.prolific.configuration;

import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SpringFXConfigurationProperties.class)
public class JavaFXConfiguration {

	@Bean(destroyMethod = "")
	public Stage primaryStage() {
		CompletableFuture<Stage> future = new CompletableFuture<>();
		Platform.runLater(() -> future.complete(new Stage()));
		return future.join();
	}
}
