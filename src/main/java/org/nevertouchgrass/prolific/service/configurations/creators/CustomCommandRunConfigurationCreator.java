package org.nevertouchgrass.prolific.service.configurations.creators;

import lombok.Data;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.configurations.creators.contract.RunConfigurationCreator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomCommandRunConfigurationCreator implements RunConfigurationCreator<CustomCommandRunConfigurationCreator.CustomCommandDescription> {
    @Override
    public RunConfig createRunConfig(CustomCommandDescription description) {
        var runConfig = new RunConfig();
        runConfig.setConfigName(description.getTitle());
        runConfig.setType("Command");
        var command = new ArrayList<>(description.command);
        runConfig.setCommand(command);
        return runConfig;
    }

    @Data
    public static class CustomCommandDescription {
        String title;
        List<String> command;
    }
}
