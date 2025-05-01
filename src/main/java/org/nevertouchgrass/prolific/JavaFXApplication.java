package org.nevertouchgrass.prolific;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import org.nevertouchgrass.prolific.events.JavaFxStartEvent;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.nevertouchgrass.prolific.events.StageShowEvent;
import org.nevertouchgrass.prolific.javafxcontroller.HeaderController;
import org.nevertouchgrass.prolific.service.ProlificPreLoader;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Setter(onMethod_ = @Autowired)
    private ProlificPreLoader preLoader;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public JavaFXApplication(@Qualifier("primaryStage") Stage primaryStage, Parent mainScreenParent, HeaderController headerController,
                             ApplicationEventPublisher applicationEventPublisher, UserSettingsService userSettingsService) {
        this.primaryStage = primaryStage;
        this.mainScreenParent = mainScreenParent;
        this.headerController = headerController;
        this.applicationEventPublisher = applicationEventPublisher;
        this.userSettingsService = userSettingsService;
    }

    @Override
    public void run(ApplicationArguments args) {
        Platform.runLater(() -> {
            if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                preLoader.start(new Stage());
            }

            applicationEventPublisher.publishEvent(new JavaFxStartEvent(this));
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            Scene scene = new Scene(mainScreenParent, visualBounds.getMaxX() / 1.5, visualBounds.getMaxY() / 1.5);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());
            primaryStage.getIcons().add(
                    new Image(Objects.requireNonNull(getClass().getResource("/icons/png/icon.png")).toExternalForm())
            );

            primaryStage.setScene(scene);
            scene.setFill(Color.TRANSPARENT);
            applicationEventPublisher.publishEvent(new StageInitializeEvent("primaryStage"));
            primaryStage.show();
            applicationEventPublisher.publishEvent(new StageShowEvent("primaryStage"));
        });
    }
}
