package org.nevertouchgrass.prolific.service.permissions;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.constants.action.DeleteProjectAction;
import org.nevertouchgrass.prolific.constants.action.IAction;
import org.nevertouchgrass.prolific.constants.profile.PowerUser;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectDeletePermissionChecker extends DefaultActionPermissionChecker<DeleteProjectAction> {
    private final UserSettingsHolder userSettingsHolder;
    @Override
    public boolean hasPermission(DeleteProjectAction action) {
        return userSettingsHolder.getUser() instanceof PowerUser;
    }

    @Override
    public Class<? extends IAction> getAction() {
        return DeleteProjectAction.class;
    }
}
