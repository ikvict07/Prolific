package org.nevertouchgrass.prolific.service;

import javafx.scene.Parent;
import org.nevertouchgrass.prolific.model.FxmlLoadedResource;

public interface FxmlProvider {
    <T> FxmlLoadedResource<T> getFxmlResource(String parentName);
    Parent getIcon(String parentName);
}
