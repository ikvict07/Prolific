package org.nevertouchgrass.prolific.service.notification.contract;

import org.nevertouchgrass.prolific.model.notification.contract.Notification;

public interface NotificationListener <T extends Notification> {
    void onNotification(T notification);
}
