package org.nevertouchgrass.prolific.service;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.constants.action.DeleteProjectAction;
import org.nevertouchgrass.prolific.constants.action.ExcludeProjectAction;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.nevertouchgrass.prolific.service.permissions.contract.PermissionChecker;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ProjectDeleteService {
    private final PermissionRegistry permissionRegistry;
    private final NotificationService notificationService;
    private final ProjectExcluderService projectExcluderService;
    private final ProcessService processService;
    private final LocalizationProvider localizationProvider;


    public void deleteProject(DeleteProjectAction action) {
        var permissionChecker = permissionRegistry.getChecker(action.getClass());
        if (permissionChecker != null) {
            @SuppressWarnings("unchecked")
            PermissionChecker<DeleteProjectAction> castedChecker = (PermissionChecker<DeleteProjectAction>) permissionChecker;

            if (castedChecker.hasPermission(action)) {
                var observableLiveProcesses = processService.getObservableLiveProcesses().get(action.project());
                if (observableLiveProcesses != null && !observableLiveProcesses.isEmpty()) {
                    notificationService.notifyError(ErrorNotification.of(null, localizationProvider.log_error_cant_delete_project_while_running()));
                    return;
                }
                projectExcluderService.excludeProject(new ExcludeProjectAction(action.project()));

                deleteDirectory(Path.of(action.project().getPath()));
                notificationService.notifyInfo(InfoNotification.of(localizationProvider.log_info_deleted_project(), action.project().getTitle()));
            } else {
                notificationService.notifyInfo(new InfoNotification(localizationProvider.log_info_cant_delete_project_permission()));
            }
        }
    }

    private void deleteDirectory(Path directory) {
        if (Files.exists(directory)) {
            try (var dir = Files.walk(directory)) {
                dir.sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                notificationService.notifyInfo(new InfoNotification(localizationProvider.log_error_deleting_project(), path));
                            }
                        });
            } catch (IOException e) {
                notificationService.notifyInfo(new InfoNotification(localizationProvider.log_error_deleting_project(), directory));
            }
        }
    }
}
