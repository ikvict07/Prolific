package org.nevertouchgrass.prolific.annotation;

import org.nevertouchgrass.prolific.constants.ActionTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnAction {
    ActionTypes value();
}

