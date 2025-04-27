package org.nevertouchgrass.prolific.service.runner;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectRunnerRegistry {
    private final List<DefaultProjectRunner> projectRunners;

    public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var projectRunner = projectRunners.stream()
                .filter(runner -> runner.getType().equalsIgnoreCase(runConfig.getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No project runner found for type: " + runConfig.getType()));
        return projectRunner.runProject(project, runConfig);
    }
}
