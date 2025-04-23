package org.nevertouchgrass.prolific.service.runner;

import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.springframework.stereotype.Service;

@Service
public class PythonProjectRunner extends ConfigurableProjectRunner {
    public PythonProjectRunner(ProcessLogsService processLogsService, MetricsService metricsService, ConfiguratorService configuratorService, UserSettingsHolder userSettingsHolder) {
        super(processLogsService, metricsService, configuratorService, userSettingsHolder);
    }

    @Override
    public RunConfig configure(RunConfig runConfig) {
        return configuratorService.configure(runConfig, userSettingsHolder.getPythonPath(), "python");
    }

    @Override
    public String getType() {
        return "Python";
    }
}
