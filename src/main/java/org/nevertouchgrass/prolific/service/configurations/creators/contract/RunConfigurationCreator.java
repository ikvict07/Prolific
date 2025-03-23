package org.nevertouchgrass.prolific.service.configurations.creators.contract;

import org.nevertouchgrass.prolific.model.RunConfig;

public interface RunConfigurationCreator <T> {
    RunConfig createRunConfig(T description);
}
