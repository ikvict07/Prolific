package org.nevertouchgrass.prolific.service;

import org.nevertouchgrass.prolific.model.FxmlLoadedResource;

public interface FxmlProvider {
    <T> FxmlLoadedResource<T> getFxmlResource(String parentName);
}
