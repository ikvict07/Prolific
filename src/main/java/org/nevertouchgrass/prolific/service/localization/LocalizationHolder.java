package org.nevertouchgrass.prolific.service.localization;

import jakarta.annotation.PostConstruct;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LocalizationHolder implements ApplicationListener<LocalizationChangeEvent> {
    private final LocalizationManager localizationManager;
    private final Map<String, StringProperty> localizationMap = new ConcurrentHashMap<>();
    private final UserSettingsHolder userSettingsHolder;

    @PostConstruct
    public void init() {
        Properties properties = localizationManager.getProperties(userSettingsHolder.getLocale());
        properties.forEach((key, value) -> localizationMap.put(key.toString(), new SimpleStringProperty(value.toString())));
    }

    public StringProperty getLocalization(String key) {
        if (!localizationMap.containsKey(key)) {
            var property = localizationManager.getProperties(Locale.forLanguageTag("en")).getProperty(key);
            if (property != null) {
                localizationMap.put(key, new SimpleStringProperty(property));
            } else {
                localizationMap.put(key, new SimpleStringProperty(localizationManager.get(key)));
            }
        }
        return localizationMap.get(key);
    }

    @Override
    public void onApplicationEvent(LocalizationChangeEvent event) {
        localizationManager.setLocale(event.getLocale());
        localizationMap.keySet().forEach(key -> localizationMap.get(key).set(localizationManager.get(key)));
    }
}
