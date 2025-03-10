package org.nevertouchgrass.prolific.service.xml.contract;

import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;

import java.util.List;

public interface ConfigImporter {
    List<RunConfig> importConfig(Project project);
}
