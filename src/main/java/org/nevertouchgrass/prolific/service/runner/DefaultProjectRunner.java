package org.nevertouchgrass.prolific.service.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.runner.contract.ProjectRunner;
import org.nevertouchgrass.prolific.util.ProcessWrapper;

import java.io.IOException;
import java.nio.file.Path;

@RequiredArgsConstructor
@Log4j2
public abstract class DefaultProjectRunner implements ProjectRunner {

    private final ProcessLogsService processLogsService;
    private final MetricsService metricsService;

    public abstract String getType();

    @Override
        public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var processBuilder = new ProcessBuilder(runConfig.getCommand());
        processBuilder.directory(Path.of(project.getPath()).toFile());
        try {
            Process process = processBuilder.start();
            log.info("Started process {}", process.pid());
            ProcessWrapper procWrapper;
            procWrapper = ProcessWrapper.of(process);
            procWrapper.setName(runConfig.getConfigName());
            processLogsService.observeProcess(procWrapper);
            metricsService.observeProcess(procWrapper);
            return procWrapper;
        } catch (IOException e) {
            throw new ProcessStartFailedException("Error occurred while starting the process " + processBuilder, e);
        }
    }
}
