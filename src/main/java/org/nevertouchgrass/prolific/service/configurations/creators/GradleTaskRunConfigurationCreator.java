package org.nevertouchgrass.prolific.service.configurations.creators;

import lombok.Data;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.configurations.creators.contract.RunConfigurationCreator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GradleTaskRunConfigurationCreator implements RunConfigurationCreator<GradleTaskRunConfigurationCreator.GradleTaskDescription> {

    @Data
    public static class GradleTaskDescription {
        String title;
        String taskName;
        List<String> options;
    }

    @Override
    public RunConfig createRunConfig(GradleTaskDescription description) {
        var runConfig = new RunConfig();
        var command = new ArrayList<String>();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            command.add("cmd");
            command.add("/c");
        }
        command.add(isWindows ? "gradlew.bat" : "./gradlew");
        command.add(description.getTaskName());
        command.addAll(description.getOptions());
        command.add("--no-daemon");
        runConfig.setCommand(command);
        runConfig.setType("Gradle");
        runConfig.setConfigName(description.getTitle());
        return runConfig;
    }
}
