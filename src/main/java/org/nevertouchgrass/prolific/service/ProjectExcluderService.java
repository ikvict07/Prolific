package org.nevertouchgrass.prolific.service;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.constants.action.ExcludeProjectAction;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.nevertouchgrass.prolific.service.permissions.contract.PermissionChecker;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectExcluderService {
    private final UserSettingsService userSettingsService;
    private final UserSettingsHolder userSettingsHolder;
    private final PermissionRegistry permissionRegistry;
    private final NotificationService notificationService;
    private final ProjectsRepository projectsRepository;
    private final ProcessService processService;
    private final LocalizationProvider localizationProvider;

    public void excludeProject(ExcludeProjectAction action) {
        var permissionChecker = permissionRegistry.getChecker(action.getClass());
        if (permissionChecker != null) {
            @SuppressWarnings("unchecked")
            PermissionChecker<ExcludeProjectAction> castedChecker =
                    (PermissionChecker<ExcludeProjectAction>) permissionChecker;

            if (castedChecker.hasPermission(action)) {
                var observableLiveProcesses = processService.getObservableLiveProcesses().get(action.project());
                if (observableLiveProcesses != null && !observableLiveProcesses.isEmpty()) {
                    notificationService.notifyError(ErrorNotification.of(null, localizationProvider.log_error_cant_exclude_project_while_running()));
                    return;
                }
                var pathTOExclude = action.project().getPath();
                userSettingsHolder.getExcludedDirs().add(formatPath(pathTOExclude));
                userSettingsService.saveSettings();
                projectsRepository.delete(action.project());
                notificationService.notifyInfo(InfoNotification.of(localizationProvider.log_info_excluded_project(), action.project().getTitle()));
            } else {
                notificationService.notifyInfo(new InfoNotification(localizationProvider.log_info_cant_exclude_project_permission()));
            }
        }
    }

    private String formatPath(String path) {
        var os = System.getProperty("os.name");
        if (os.toLowerCase().contains("win")) {
            return path.substring(3);
        } else {
            return path.substring(1);
        }
    }
}
