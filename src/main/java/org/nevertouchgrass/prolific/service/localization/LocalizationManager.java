package org.nevertouchgrass.prolific.service.localization;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.configuration.XMLMessageSource;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class LocalizationManager {

    private final MessageSource messageSource;
    private final UserSettingsHolder userSettingsHolder;
    private final UserSettingsService userSettingsService;


    public String get(String key) {
        Locale locale = userSettingsHolder.getLocale();
        return messageSource.getMessage(key, null, locale);
    }

    public Properties getProperties(Locale locale) {
        return ((XMLMessageSource) messageSource).getProperties(locale);
    }

    public void setLocale(Locale locale) {
        userSettingsHolder.setLocale(locale);
        userSettingsService.saveSettings();
    }
}