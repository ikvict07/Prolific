package org.nevertouchgrass.prolific.annotation;

import org.nevertouchgrass.prolific.constants.ActionTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@OnAction(ActionTypes.DELETE)
public @interface OnDelete {
    Class<?> value();
}
