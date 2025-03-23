package org.nevertouchgrass.prolific.model.notification;

public class ErrorNotification implements Notification {
    private final Throwable throwable;
    private final String message;

    public static ErrorNotification of(Throwable throwable, String format, Object... args) {
        var message  = StringFormatter.formatMessage(format, args);
        return new ErrorNotification(throwable, message);
    }

    public ErrorNotification(Throwable throwable, String message) {
        this.throwable = throwable;
        this.message = message;
    }

    public record ErrorNotificationPayload(Throwable throwable, String message){}

    @Override
    public ErrorNotificationPayload getPayload() {
        return new ErrorNotificationPayload(throwable, message);
    }
}
