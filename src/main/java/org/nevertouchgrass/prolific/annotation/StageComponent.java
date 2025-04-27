package org.nevertouchgrass.prolific.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * If this component is shown on the stage -
 * this component must be annotated with this annotation
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface StageComponent {
	/**
	 *
	 * @return Name of the stage related to this component, for example, primaryStage
	 */
	String stage() default "primaryStage";
}
