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
import java.util.HashSet;
import java.util.Set;

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
            process.onExit()
                    .thenAccept(p -> {
                        var descendantsToReceive = new HashSet<ProcessHandle>();
                        receiveDescendants(process.toHandle(), descendantsToReceive);
                        descendantsToReceive.forEach(ProcessHandle::destroy);
                    });
            return procWrapper;
        } catch (IOException e) {
            throw new ProcessStartFailedException("Error occurred while starting the process " + processBuilder, e);
        }
    }
    public void receiveDescendants(ProcessHandle process, Set<ProcessHandle> descendantsToReceive) {
        var d = new HashSet<ProcessHandle>();
        var children = process.children().toList();
        var descendants = process.descendants().toList();
        d.add(process);
        d.addAll(children);
        d.addAll(descendants);
        descendantsToReceive.addAll(d);
        children.forEach(c -> receiveDescendants(c, descendantsToReceive));
        children.forEach(c -> receiveDescendants(c, descendantsToReceive));
    }
}
