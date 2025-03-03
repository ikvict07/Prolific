package org.nevertouchgrass.prolific;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.nevertouchgrass.prolific.events.JavaFxStartEvent;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.nevertouchgrass.prolific.events.StageShowEvent;
import org.nevertouchgrass.prolific.javafxcontroller.HeaderController;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * JavaFx entry point
 */

@Component
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class JavaFXApplication implements ApplicationRunner {

    private final Stage primaryStage;
    private final Parent mainScreenParent;
    private final HeaderController headerController;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final UserSettingsService userSettingsService;


    private final Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
    private final ProjectsRepository projectsRepository;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JavaFXApplication(Stage primaryStage, Parent mainScreenParent, HeaderController headerController,
                             ApplicationEventPublisher applicationEventPublisher, UserSettingsService userSettingsService, ProjectsRepository projectsRepository) {
        this.primaryStage = primaryStage;
        this.mainScreenParent = mainScreenParent;
        this.headerController = headerController;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userSettingsService = userSettingsService;
        this.projectsRepository = projectsRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Platform.runLater(() -> {
            applicationEventPublisher.publishEvent(new JavaFxStartEvent(this));
            primaryStage.initStyle(StageStyle.UNDECORATED);

            VBox root = new VBox();
            root.getChildren().addAll(mainScreenParent);

            Scene scene = new Scene(root, visualBounds.getMaxX() / 1.5, visualBounds.getMaxY() / 1.5);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

            primaryStage.setScene(scene);
            applicationEventPublisher.publishEvent(new StageInitializeEvent("primaryStage"));
            primaryStage.show();
            applicationEventPublisher.publishEvent(new StageShowEvent("primaryStage"));
        });
    }
}
