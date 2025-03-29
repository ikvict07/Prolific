package org.nevertouchgrass.prolific.model.notification;

import org.nevertouchgrass.prolific.model.notification.contract.Notification;

public class EventNotification implements Notification {
    private final EventType event;

    public EventNotification(EventType event) {
        this.event = event;
    }

    public enum EventType {
        START_PROJECT_SCAN,
        END_PROJECT_SCAN,
    }

    @Override
    public EventType getPayload() {
        return event;
    }
}
