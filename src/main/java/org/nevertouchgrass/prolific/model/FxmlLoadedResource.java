package org.nevertouchgrass.prolific.model;

import javafx.scene.Parent;
import lombok.Data;

@Data
public class FxmlLoadedResource<T> {
    private final Parent parent;
    private final T controller;
}
