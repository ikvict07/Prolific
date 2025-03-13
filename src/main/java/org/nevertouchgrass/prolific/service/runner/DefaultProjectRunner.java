package org.nevertouchgrass.prolific.service.runner;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.runner.contract.ProjectRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DefaultProjectRunner implements ProjectRunner {
    @Override
    public Process runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var processBuilder = new ProcessBuilder(runConfig.getCommand());
        processBuilder.directory(Path.of(project.getPath()).toFile());
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new ProcessStartFailedException("Error occurred while starting the process " + processBuilder, e);
        }
    }
}
