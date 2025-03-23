package org.nevertouchgrass.prolific.configuration;

import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.model.notification.EventNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.model.notification.Notification;
import org.nevertouchgrass.prolific.service.notification.contract.NotificationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ListenersConfiguration {
    @Bean
    public List<NotificationListener<ErrorNotification>> errorNotificationListeners(List<NotificationListener<ErrorNotification>> listeners) {
        return listeners;
    }
    @Bean
    public List<NotificationListener<InfoNotification>> infoNotificationListeners(List<NotificationListener<InfoNotification>> listeners) {
        return listeners;
    }
    @Bean
    public List<NotificationListener<EventNotification>> eventNotificationListeners(List<NotificationListener<EventNotification>> listeners) {
        return listeners;
    }
    @Bean
    public List<NotificationListener<Notification>> allNotificationListeners(List<NotificationListener<Notification>> listeners) {
        return listeners;
    }
}
