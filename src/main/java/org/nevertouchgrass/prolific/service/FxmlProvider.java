package org.nevertouchgrass.prolific.service;

import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.model.FxmlLoadedResource;

public interface FxmlProvider {
    @SneakyThrows
    <T> FxmlLoadedResource<T> getFxmlResource(String parentName);
}
