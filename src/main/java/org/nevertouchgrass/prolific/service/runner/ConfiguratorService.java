package org.nevertouchgrass.prolific.service.runner;

import org.nevertouchgrass.prolific.model.RunConfig;
import org.springframework.stereotype.Service;

@Service
public class ConfiguratorService {
    public RunConfig configure(RunConfig runConfig, String newCommand, String toReplace) {
        var rawCommand = runConfig.getCommand();
        var gradle = rawCommand.getFirst();
        if (gradle.contains(toReplace)) {
            if (newCommand != null && !newCommand.isEmpty()) {
                rawCommand.set(0, newCommand);
            }
        }
        var newConfig = new RunConfig();
        newConfig.setCommand(rawCommand);
        newConfig.setConfigName(runConfig.getConfigName());
        return newConfig;
    }
}
