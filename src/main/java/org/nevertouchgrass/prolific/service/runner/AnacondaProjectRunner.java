package org.nevertouchgrass.prolific.service.runner;

import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.springframework.stereotype.Service;

@Service
public class AnacondaProjectRunner extends ConfigurableProjectRunner {
    private final NotificationService notificationService;
    private final LocalizationProvider localizationProvider;
    public AnacondaProjectRunner(ProcessLogsService processLogsService, MetricsService metricsService, ConfiguratorService configuratorService, UserSettingsHolder userSettingsHolder, NotificationService notificationService, LocalizationProvider localizationProvider, PermissionRegistry permissionRegistry) {
        super(processLogsService, metricsService, configuratorService, userSettingsHolder, permissionRegistry);
        this.notificationService = notificationService;
        this.localizationProvider = localizationProvider;
    }

    @Override
    public String getType() {
        return "Anaconda";
    }

    @Override
    public RunConfig configure(RunConfig runConfig) {
        var command = runConfig.getCommand();
        var condaPart = command.getFirst();
        if (condaPart.contains("%{path}%")) {
            if (userSettingsHolder.getAnacondaPath() != null && !userSettingsHolder.getAnacondaPath().isBlank()) {
                condaPart = condaPart.replace("%{path}%", userSettingsHolder.getAnacondaPath());
                command.set(0, condaPart);
            } else {
                notificationService.notifyError(ErrorNotification.of(null, localizationProvider.no_anaconda_configured()));
                throw new IllegalArgumentException("Anaconda path is not set");
            }
        }
        runConfig.setCommand(command);
        return runConfig;
    }
}
