package org.nevertouchgrass.prolific.service.notification;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.EventNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.model.notification.Notification;
import org.nevertouchgrass.prolific.service.notification.contract.NotificationListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final List<NotificationListener<ErrorNotification>> errorListeners;
    private final List<NotificationListener<InfoNotification>> infoListeners;
    private final List<NotificationListener<EventNotification>> eventListeners;
    private final List<NotificationListener<Notification>> allListeners;

    public void notifyError(ErrorNotification notification) {
        errorListeners.forEach(l -> l.onNotification(notification));
        allListeners.forEach(l -> l.onNotification(notification));
    }
    public void notifyInfo(InfoNotification notification) {
        infoListeners.forEach(l -> l.onNotification(notification));
        allListeners.forEach(l -> l.onNotification(notification));
    }

    public void notifyEvent(EventNotification notification) {
        eventListeners.forEach(l -> l.onNotification(notification));
        allListeners.forEach(l -> l.onNotification(notification));
    }
}
