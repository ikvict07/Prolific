package org.nevertouchgrass.prolific.service.permissions.contract;

public interface PermissionChecker <T> {
    boolean hasPermission(T action);
}
