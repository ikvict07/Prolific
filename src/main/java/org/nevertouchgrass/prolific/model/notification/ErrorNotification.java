package org.nevertouchgrass.prolific.model.notification;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.nevertouchgrass.prolific.model.notification.contract.Notification;

import java.text.MessageFormat;

public class ErrorNotification implements Notification {
    private final Throwable throwable;
    private final StringProperty message;

    public static ErrorNotification of(Throwable throwable, StringProperty base, Object... args) {
        return new ErrorNotification(throwable, base, args);
    }

    public ErrorNotification(Throwable throwable, StringProperty base, Object... args) {
        this.throwable = throwable;
        this.message = new SimpleStringProperty(MessageFormat.format(base.get(), args));
        base.addListener((_, _, newValue) -> message.set(MessageFormat.format(newValue, args)));
    }

    public record ErrorNotificationPayload(Throwable throwable, StringProperty message){}

    @Override
    public ErrorNotificationPayload getPayload() {
        return new ErrorNotificationPayload(throwable, message);
    }
}
