package org.nevertouchgrass.prolific.service.permissions;

import org.nevertouchgrass.prolific.constants.action.IAction;
import org.nevertouchgrass.prolific.service.permissions.contract.PermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DefaultActionPermissionChecker <T extends IAction> implements PermissionChecker<T> {
    public abstract Class<? extends IAction> getAction();

    @Autowired
    private void register(PermissionRegistry registry) {
        registry.registerChecker(getAction(), this);
    }
}
