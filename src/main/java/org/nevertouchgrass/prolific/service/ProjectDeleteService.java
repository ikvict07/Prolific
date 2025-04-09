package org.nevertouchgrass.prolific.service;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.constants.action.DeleteProjectAction;
import org.nevertouchgrass.prolific.constants.action.ExcludeProjectAction;
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


    public void deleteProject(DeleteProjectAction action) {
        var permissionChecker = permissionRegistry.getChecker(action.getClass());
        if (permissionChecker != null) {
            @SuppressWarnings("unchecked")
            PermissionChecker<DeleteProjectAction> castedChecker = (PermissionChecker<DeleteProjectAction>) permissionChecker;

            if (castedChecker.hasPermission(action)) {
                if (!processService.getObservableLiveProcesses().get(action.project()).isEmpty()) {
//                    notificationService.notifyError(ErrorNotification.of(null, "You can't exclude while process is running")); TODO: localize
                    return;
                }
                projectExcluderService.excludeProject(new ExcludeProjectAction(action.project()));

                deleteDirectory(Path.of(action.project().getPath()));
//                notificationService.notifyInfo(InfoNotification.of("{} deleted", action.project().getTitle())); TODO: localize
            } else {
//                notificationService.notifyInfo(new InfoNotification("You don't have permission to delete this project")); TODO: localize
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
//                                notificationService.notifyInfo(new InfoNotification("Error occurred while deleting " + path)); TODO: localize
                            }
                        });
            } catch (IOException e) {
//                notificationService.notifyInfo(new InfoNotification("Error occurred while deleting " + directory)); TODO: localize
            }
        }
    }
}
