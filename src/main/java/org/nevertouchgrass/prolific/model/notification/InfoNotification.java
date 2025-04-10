package org.nevertouchgrass.prolific.model.notification;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.ToString;
import org.nevertouchgrass.prolific.model.notification.contract.Notification;

import java.text.MessageFormat;

@ToString
public class InfoNotification implements Notification {
    private final StringProperty base;
    private final Object[] args;
    private final StringProperty message;

    public static InfoNotification of(StringProperty format, Object... args) {
        return new InfoNotification(format, args);
    }

    public InfoNotification(StringProperty base, Object... args) {
        this.base = base;
        this.args = args;
        this.message = new SimpleStringProperty(MessageFormat.format(base.get(), args));
        base.addListener((_, _, newValue) -> message.set(MessageFormat.format(newValue, args)));
    }

    @Override
    public StringProperty getPayload() {
        return message;
    }
}
