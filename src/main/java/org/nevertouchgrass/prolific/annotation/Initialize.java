package org.nevertouchgrass.prolific.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Method marked with this annotation will be called, when stage, related to this class is initialized
 *
 * @see org.nevertouchgrass.prolific.listener.InitializeAnnotationProcessor
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Initialize {
}
