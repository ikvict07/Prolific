package org.nevertouchgrass.prolific.service.runner;

import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.springframework.stereotype.Service;

@Service
public class CommandProjectRunner extends DefaultProjectRunner {
    public CommandProjectRunner(ProcessLogsService processLogsService, MetricsService metricsService) {
        super(processLogsService, metricsService);
    }

    @Override
    public String getType() {
        return "Command";
    }
}
