package org.nevertouchgrass.prolific.service.notification;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.EventNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.model.notification.Notification;
import org.nevertouchgrass.prolific.service.notification.contract.NotificationListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Lazy
public class NotificationService {
    private final ObjectProvider<List<NotificationListener<ErrorNotification>>> errorListeners;
    private final ObjectProvider<List<NotificationListener<InfoNotification>>> infoListeners;
    private final ObjectProvider<List<NotificationListener<EventNotification>>> eventListeners;
    private final ObjectProvider<List<NotificationListener<Notification>>> allListeners;

    public void notifyError(ErrorNotification notification) {
        errorListeners.getObject().forEach(l -> l.onNotification(notification));
        allListeners.getObject().forEach(l -> l.onNotification(notification));
    }

    public void notifyInfo(InfoNotification notification) {
        infoListeners.getObject().forEach(l -> l.onNotification(notification));
        allListeners.getObject().forEach(l -> l.onNotification(notification));
    }

    public void notifyEvent(EventNotification notification) {
        eventListeners.getObject().forEach(l -> l.onNotification(notification));
        allListeners.getObject().forEach(l -> l.onNotification(notification));
    }
}
