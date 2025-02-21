package org.nevertouchgrass.prolific;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nevertouchgrass.springfx.event.JavaFxStartEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class JavaFXApplication implements ApplicationRunner {

	private final Stage primaryStage;

	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public JavaFXApplication(Stage primaryStage, ApplicationEventPublisher applicationEventPublisher) {
		this.primaryStage = primaryStage;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void run(ApplicationArguments args) {
		Platform.runLater(() -> {
			primaryStage.initStyle(StageStyle.UNDECORATED);

			VBox root = new VBox();
			root.getChildren().addAll(new VBox());

			Scene scene = new Scene(root, 1980, 1080);
			primaryStage.setScene(scene);
			applicationEventPublisher.publishEvent(new JavaFxStartEvent(this));
			primaryStage.show();
		});
	}
}
