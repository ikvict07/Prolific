package org.nevertouchgrass.prolific.service.localization;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.configuration.XMLMessageSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class LocalizationManager {

    private final MessageSource messageSource;

    public String get(String key) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, null, locale);
    }

    public Properties getProperties(Locale locale) {
        return ((XMLMessageSource) messageSource).getProperties(locale);
    }

    public void setLocale(Locale locale) {
        LocaleContextHolder.setLocale(locale);
    }
}