package org.nevertouchgrass.prolific.service.configurations.creators;

import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.configurations.creators.contract.RunConfigurationCreator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MavenRunConfigurationCreator implements RunConfigurationCreator<MavenRunConfigurationCreator.MavenDescription> {

    @Override
    public RunConfig createRunConfig(MavenDescription description) {
        var runConfig = new RunConfig();
        runConfig.setConfigName(description.title);
        runConfig.setType("Maven");
        var command = new ArrayList<String>();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        command.add(isWindows ? "mvn.cmd" : "mvn");
        command.add(description.goal);
        if (description.options != null && !description.options.isEmpty()) {
            command.addAll(description.options);
        }
        runConfig.setCommand(command);
        return runConfig;
    }

    public static class MavenDescription {
        String title;
        String goal;
        List<String> options;
    }
}
