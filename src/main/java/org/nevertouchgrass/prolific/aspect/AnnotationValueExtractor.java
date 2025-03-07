package org.nevertouchgrass.prolific.aspect;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface AnnotationValueExtractor<T extends Annotation, R> {
    R extract(T annotation);
}
