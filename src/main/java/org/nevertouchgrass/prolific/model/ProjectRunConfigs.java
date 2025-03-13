package org.nevertouchgrass.prolific.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectRunConfigs {
    private List<RunConfig> importedConfigs;
    private List<RunConfig> manuallyAddedConfigs;
}
