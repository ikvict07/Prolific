package org.nevertouchgrass.prolific.model.notification;

import lombok.ToString;
import org.nevertouchgrass.prolific.model.notification.contract.Notification;

@ToString
public class InfoNotification implements Notification {
    private final String message;

    public static InfoNotification of(String format, Object... args) {
        var message  = StringFormatter.formatMessage(format, args);
        return new InfoNotification(message);
    }

    public InfoNotification(String message) {
        this.message = message;
    }

    @Override
    public String getPayload() {
        return message;
    }
}
