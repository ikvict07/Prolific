package org.nevertouchgrass.prolific.service.runner;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.runner.contract.ProjectRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DefaultProjectRunner implements ProjectRunner {

    private ProcessLogsService processLogsService;

    @Autowired
    public void set(ProcessLogsService processLogsService) {
        this.processLogsService = processLogsService;
    }

    @Override
    public Process runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var processBuilder = new ProcessBuilder(runConfig.getCommand());
        processBuilder.directory(Path.of(project.getPath()).toFile());
        try {
            Process process = processBuilder.start();
            processLogsService.observeProcess(process);
            return process;
        } catch (IOException e) {
            throw new ProcessStartFailedException("Error occurred while starting the process " + processBuilder, e);
        }
    }
}
