package org.nevertouchgrass.prolific.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Constraints annotation is used to define layout constraints for UI elements.
 * Properties: specify in percent how much to retreat from the corresponding edge
 * This annotation will consider element's sizes
 * @see ConstraintsIgnoreElementSize
 * @see org.nevertouchgrass.prolific.configuration.ConstraintsAnnotationProcessor
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Constraints {
    double top() default -1;

    double right() default -1;

    double bottom() default -1;

    double left() default -1;
}
