package org.nevertouchgrass.prolific.configuration;

import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.AnchorPaneController;
import org.nevertouchgrass.prolific.events.StageShowEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@SuppressWarnings("java:S3011")
@Log4j2
public class AnchorPaneAutomaticSizeBinder implements ApplicationListener<StageShowEvent> {

    private final ApplicationContext applicationContext;

    public AnchorPaneAutomaticSizeBinder(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void bind() {
        applicationContext.getBeansWithAnnotation(AnchorPaneController.class).forEach((_, bean) -> Arrays.stream(bean.getClass().getDeclaredFields()).filter(field -> AnchorPane.class.isAssignableFrom(field.getType())).forEach(field -> {
            field.setAccessible(true);
            try {
                AnchorPane anchorPane = (AnchorPane) field.get(bean);
                Parent p = anchorPane.getParent();
                if (p == null) {
                    return;
                }
                if (!(p instanceof Region region)) {
                    return;
                }
                anchorPane.prefWidthProperty().bind(region.widthProperty());
                anchorPane.minWidthProperty().bind(region.widthProperty());
                anchorPane.maxWidthProperty().bind(region.widthProperty());

                anchorPane.prefHeightProperty().bind(anchorPane.heightProperty());
                anchorPane.minHeightProperty().bind(anchorPane.heightProperty());
                anchorPane.maxHeightProperty().bind(anchorPane.heightProperty());
            } catch (IllegalAccessException e) {
                log.error("Error while binding AnchorPane size, application will continue it's execution", e);
            }
        }));
    }

    @Override
    public void onApplicationEvent(@NonNull StageShowEvent event) {
        bind();
    }
}
