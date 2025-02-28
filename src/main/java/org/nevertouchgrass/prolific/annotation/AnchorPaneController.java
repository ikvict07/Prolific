package org.nevertouchgrass.prolific.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;


/**
 * This annotation should be used for all controllers, that has AnchorPane as a root element
 * @see org.nevertouchgrass.prolific.service.AnchorPaneConstraintsService
 * @see org.nevertouchgrass.prolific.configuration.ConstraintsAnnotationProcessor
 */
@Component
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface AnchorPaneController {
}
