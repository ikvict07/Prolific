package org.nevertouchgrass.prolific.service.runner;

import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.springframework.stereotype.Service;

@Service
public class MavenProjectRunner extends ConfigurableProjectRunner {
    public MavenProjectRunner(ProcessLogsService processLogsService, MetricsService metricsService, ConfiguratorService configuratorService, UserSettingsHolder userSettingsHolder, PermissionRegistry permissionRegistry) {
        super(processLogsService, metricsService, configuratorService, userSettingsHolder, permissionRegistry);
    }

    @Override
    public RunConfig configure(RunConfig runConfig) {
        var newConfig = configuratorService.configure(runConfig, userSettingsHolder.getMavenPath(), "mvn");
        if (userSettingsHolder.getJdkPath() != null && !userSettingsHolder.getJdkPath().isBlank()) {
            var newCommand = newConfig.getCommand();
            newCommand.addLast("-Djava.home=" + userSettingsHolder.getJdkPath());
            newConfig.setCommand(newCommand);
        }
        return newConfig;
    }

    @Override
    public String getType() {
        return "Maven";
    }
}
