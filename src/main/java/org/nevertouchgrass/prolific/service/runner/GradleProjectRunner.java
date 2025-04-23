package org.nevertouchgrass.prolific.service.runner;

import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class GradleProjectRunner extends ConfigurableProjectRunner {
    public GradleProjectRunner(ProcessLogsService processLogsService, MetricsService metricsService, UserSettingsHolder userSettingsHolder, ConfiguratorService configuratorService) {
        super(processLogsService, metricsService, configuratorService, userSettingsHolder);
    }
    @Override
    public String getType() {
        return "Gradle";
    }
    @Override
    public RunConfig configure(RunConfig runConfig) {
        var newConfig = configuratorService.configure(runConfig, userSettingsHolder.getGradlePath(), "gradle");
        if (userSettingsHolder.getJdkPath() != null && !userSettingsHolder.getJdkPath().isBlank()) {
            var newCommand = newConfig.getCommand();
            newCommand.addLast("-Dorg.gradle.java.home=" + userSettingsHolder.getJdkPath());
            newConfig.setCommand(newCommand);
        }
        return newConfig;
    }
}
