package org.nevertouchgrass.prolific;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nevertouchgrass.prolific.events.JavaFxStartEvent;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.nevertouchgrass.prolific.javafxcontroller.HeaderController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class JavaFXApplication implements ApplicationRunner {

	private final Stage primaryStage;
	private final Parent mainScreenParent;
	private final HeaderController headerController;

	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
	public JavaFXApplication(Stage primaryStage, Parent mainScreenParent, HeaderController headerController,
			ApplicationEventPublisher applicationEventPublisher) {
		this.primaryStage = primaryStage;
		this.mainScreenParent = mainScreenParent;
		this.headerController = headerController;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void run(ApplicationArguments args) {
		Platform.runLater(() -> {
			applicationEventPublisher.publishEvent(new JavaFxStartEvent(this));
			primaryStage.initStyle(StageStyle.UNDECORATED);

			VBox root = new VBox();
			root.getChildren().addAll(mainScreenParent);

			Scene scene = new Scene(root, 1980, 1080);
			primaryStage.setScene(scene);
			applicationEventPublisher.publishEvent(new StageInitializeEvent("primaryStage"));
			primaryStage.show();
		});
	}
}
