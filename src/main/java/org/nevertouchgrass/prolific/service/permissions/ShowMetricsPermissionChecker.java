package org.nevertouchgrass.prolific.service.permissions;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.constants.action.IAction;
import org.nevertouchgrass.prolific.constants.action.SeeMetricsAction;
import org.nevertouchgrass.prolific.constants.profile.NoMetricsUser;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShowMetricsPermissionChecker extends DefaultActionPermissionChecker<SeeMetricsAction> {
    private final UserSettingsHolder userSettingsHolder;
    @Override
    public Class<? extends IAction> getAction() {
        return SeeMetricsAction.class;
    }

    @Override
    public boolean hasPermission(SeeMetricsAction action) {
        return !(userSettingsHolder.getUser() instanceof NoMetricsUser);
    }
}
