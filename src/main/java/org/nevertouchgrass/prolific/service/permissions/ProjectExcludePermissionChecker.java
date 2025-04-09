package org.nevertouchgrass.prolific.service.permissions;

import org.nevertouchgrass.prolific.constants.action.ExcludeProjectAction;
import org.nevertouchgrass.prolific.constants.action.IAction;
import org.springframework.stereotype.Service;

@Service
public class ProjectExcludePermissionChecker extends DefaultActionPermissionChecker<ExcludeProjectAction> {
    @Override
    public boolean hasPermission(ExcludeProjectAction action) {
        return true;
    }

    @Override
    public Class<? extends IAction> getAction() {
        return ExcludeProjectAction.class;
    }
}
