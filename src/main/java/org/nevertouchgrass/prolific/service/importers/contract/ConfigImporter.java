package org.nevertouchgrass.prolific.service.importers.contract;

import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;

import java.util.List;

public interface ConfigImporter {
    List<RunConfig> importConfig(Project project);

    String getType();
    default boolean supports(Project project) {
        return getType().equalsIgnoreCase(project.getType());
    }
}
