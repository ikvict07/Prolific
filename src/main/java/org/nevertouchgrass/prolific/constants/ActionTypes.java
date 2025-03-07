package org.nevertouchgrass.prolific.constants;

import org.nevertouchgrass.prolific.annotation.OnDelete;
import org.nevertouchgrass.prolific.annotation.OnSave;
import org.nevertouchgrass.prolific.annotation.OnUpdate;
import org.nevertouchgrass.prolific.aspect.AnnotationValueExtractor;

import java.lang.annotation.Annotation;

public enum ActionTypes {
    SAVE((annotation -> ((OnSave) annotation).value())),

    DELETE((annotation -> ((OnDelete) annotation).value())),

    UPDATE((annotation -> ((OnUpdate) annotation).value()));

    public final AnnotationValueExtractor<Annotation, Class<?>> extractor;

    ActionTypes(AnnotationValueExtractor<Annotation, Class<?>> extractor) {
        this.extractor = extractor;
    }
}
