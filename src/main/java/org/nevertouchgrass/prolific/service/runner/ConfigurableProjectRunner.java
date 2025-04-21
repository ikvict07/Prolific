package org.nevertouchgrass.prolific.service.runner;

import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;

public abstract class ConfigurableProjectRunner extends DefaultProjectRunner {
    protected final ConfiguratorService configuratorService;
    protected final UserSettingsHolder userSettingsHolder;
    public ConfigurableProjectRunner(ProcessLogsService processLogsService, MetricsService metricsService, ConfiguratorService configuratorService, UserSettingsHolder userSettingsHolder) {
        super(processLogsService, metricsService);
        this.configuratorService = configuratorService;
        this.userSettingsHolder = userSettingsHolder;
    }

    @Override
    public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var config = configure(runConfig);
        return super.runProject(project, config);
    }

    public abstract RunConfig configure(RunConfig runConfig);
}
