package org.nevertouchgrass.prolific.service.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.constants.action.SeeMetricsAction;
import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.nevertouchgrass.prolific.service.runner.contract.ProjectRunner;
import org.nevertouchgrass.prolific.util.ProcessWrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

import static org.nevertouchgrass.prolific.util.ProcessUtl.receiveDescendants;

@RequiredArgsConstructor
@Log4j2
public abstract class DefaultProjectRunner implements ProjectRunner {

    private final ProcessLogsService processLogsService;
    private final MetricsService metricsService;
    private final PermissionRegistry permissionRegistry;

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
            if (permissionRegistry.getChecker(SeeMetricsAction.class).hasPermission(new SeeMetricsAction())) {
                metricsService.observeProcess(procWrapper);
            }
            process.onExit()
                    .thenAccept(_ -> {
                        var descendantsToReceive = new HashSet<ProcessHandle>();
                        receiveDescendants(process.toHandle(), descendantsToReceive);
                        descendantsToReceive.forEach(ProcessHandle::destroy);
                    });
            return procWrapper;
        } catch (IOException e) {
            throw new ProcessStartFailedException("Error occurred while starting the process " + processBuilder, e);
        }
    }
}
