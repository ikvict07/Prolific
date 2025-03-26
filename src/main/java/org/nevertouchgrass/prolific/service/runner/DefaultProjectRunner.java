package org.nevertouchgrass.prolific.service.runner;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.runner.contract.ProjectRunner;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class DefaultProjectRunner implements ProjectRunner {

    private final ProcessLogsService processLogsService;
    private final MetricsService metricsService;


    @Override
    public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var processBuilder = new ProcessBuilder(runConfig.getCommand());
        processBuilder.directory(Path.of(project.getPath()).toFile());
        try {
            Process process = processBuilder.start();
            var procWrapper = ProcessWrapper.of(process);
            procWrapper.setName(runConfig.getConfigName());
            processLogsService.observeProcess(procWrapper);
            metricsService.observeProcess(procWrapper);
            return procWrapper;
        } catch (IOException e) {
            throw new ProcessStartFailedException("Error occurred while starting the process " + processBuilder, e);
        }
    }
}
