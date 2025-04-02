package org.nevertouchgrass.prolific.service.permissions;

import org.nevertouchgrass.prolific.constants.action.IAction;
import org.nevertouchgrass.prolific.service.permissions.contract.PermissionChecker;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PermissionRegistry {
    private final Map<Class<? extends IAction>, PermissionChecker<?>> checkers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T extends IAction> PermissionChecker<T> getChecker(Class<T> action) {
        return (PermissionChecker<T>) checkers.get(action);
    }

    public void registerChecker(Class<? extends IAction> action, PermissionChecker<?> checker) {
        checkers.put(action, checker);
    }
}
