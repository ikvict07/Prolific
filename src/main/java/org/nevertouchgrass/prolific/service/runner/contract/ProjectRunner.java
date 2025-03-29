package org.nevertouchgrass.prolific.service.runner.contract;

import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.util.ProcessWrapper;

public interface ProjectRunner {
    ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException;
}
