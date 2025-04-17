package org.nevertouchgrass.prolific.service.permissions;

import org.nevertouchgrass.prolific.constants.action.DeleteProjectAction;
import org.nevertouchgrass.prolific.constants.action.IAction;
import org.springframework.stereotype.Service;

@Service
public class ProjectDeletePermissionChecker extends DefaultActionPermissionChecker<DeleteProjectAction> {
    @Override
    public boolean hasPermission(DeleteProjectAction action) {
        return true;
    }

    @Override
    public Class<? extends IAction> getAction() {
        return DeleteProjectAction.class;
    }
}
