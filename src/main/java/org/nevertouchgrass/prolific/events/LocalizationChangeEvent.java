package org.nevertouchgrass.prolific.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Locale;

public class LocalizationChangeEvent extends ApplicationEvent {
    public LocalizationChangeEvent(Object source, Locale locale) {
        super(source);
        this.locale = locale;
    }
    @Getter
    private final Locale locale;
}
