package org.nevertouchgrass.prolific.configuration;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.nevertouchgrass.prolific.annotation.AnchorPaneController;
import org.nevertouchgrass.prolific.annotation.Constraints;
import org.nevertouchgrass.prolific.annotation.ConstraintsIgnoreElementSize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.nevertouchgrass.prolific.service.AnchorPaneConstraintsService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ConstraintsAnnotationProcessor implements ApplicationListener<StageInitializeEvent> {
    private final ApplicationContext applicationContext;
    private final ObjectFactory<AnchorPaneConstraintsService> anchorPaneConstraintsServiceProvider;

    public ConstraintsAnnotationProcessor(ApplicationContext applicationContext,
                                          ObjectFactory<AnchorPaneConstraintsService> anchorPaneConstraintsService) {
        this.applicationContext = applicationContext;
        this.anchorPaneConstraintsServiceProvider = anchorPaneConstraintsService;
    }

    @Override
    public void onApplicationEvent(StageInitializeEvent event) {
        applicationContext.getBeansWithAnnotation(AnchorPaneController.class).forEach((name, bean) -> {
            if (!bean.getClass().isAnnotationPresent(StageComponent.class)) {
                return;
            }

            StageComponent stageComponent = bean.getClass().getAnnotation(StageComponent.class);

            if (!event.getStage().equals(stageComponent.value())) {
                return;
            }

            var fields = bean.getClass().getDeclaredFields();
            var anchorPaneConstraintsService = anchorPaneConstraintsServiceProvider.getObject();
            for (var field : fields) {
                if (field.getType() == Stage.class) {
                    field.setAccessible(true);
                    try {
                        anchorPaneConstraintsService.setStage(((Stage) field.get(bean)));
                        break;
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
            for (var field : fields) {
                if (field.isAnnotationPresent(Constraints.class)) {
                    field.setAccessible(true);
                    try {
                        processConstraints((Node) field.get(bean), field.getAnnotation(Constraints.class),
                                anchorPaneConstraintsService);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (field.isAnnotationPresent(ConstraintsIgnoreElementSize.class)) {
                    field.setAccessible(true);
                    try {
                        processConstraintsIgnoreElementSize((Node) field.get(bean),
                                field.getAnnotation(ConstraintsIgnoreElementSize.class), anchorPaneConstraintsService);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void processConstraints(Node node, Constraints constraints,
                                    AnchorPaneConstraintsService anchorPaneConstraintsService) {
        if (constraints.top() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsTop(node, constraints.top());
        }
        if (constraints.right() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsRight(node, constraints.right());
        }
        if (constraints.bottom() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsBottom(node, constraints.bottom());
        }
        if (constraints.left() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsLeft(node, constraints.left());
        }

    }

    private void processConstraintsIgnoreElementSize(Node node, ConstraintsIgnoreElementSize constraints,
                                                     AnchorPaneConstraintsService anchorPaneConstraintsService) {
        if (constraints.top() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeTop(node, constraints.top());
        }
        if (constraints.right() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeRight(node, constraints.right());
        }
        if (constraints.bottom() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeBottom(node, constraints.bottom());
        }
        if (constraints.left() != -1) {
            anchorPaneConstraintsService.setAnchorConstraintsIgnoreElementSizeLeft(node, constraints.left());
        }
    }


}
