package org.nevertouchgrass.prolific.localization;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LocalizationManager {

    private final MessageSource messageSource;

    public LocalizationManager(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String get(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, "[Missing: " + key + "]", locale);
    }

    public void setLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
        System.out.println("Locale set to: " + locale);
    }
}

