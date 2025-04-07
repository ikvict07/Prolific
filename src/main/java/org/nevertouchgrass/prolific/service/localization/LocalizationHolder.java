package org.nevertouchgrass.prolific.service.localization;

import jakarta.annotation.PostConstruct;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class LocalizationHolder implements ApplicationListener<LocalizationChangeEvent> {
    private final ResourceLoader resourceLoader;
    private final LocalizationManager localizationManager;
    private final Map<String, StringProperty> localizationMap = new ConcurrentHashMap<>();

    @PostConstruct
    @SneakyThrows
    public void init() {
        var props = new BufferedReader(new InputStreamReader(resourceLoader.getResource("classpath:" + "messages.properties").getInputStream()));
        props.lines().forEach(line -> {
            var propName = line.split("=")[0];
            var propValue = localizationManager.get(propName);
            var ssp = new SimpleStringProperty(propValue);
            localizationMap.put(propName, ssp);
        });
    }

    public StringProperty getLocalization(String key) {
        if (!localizationMap.containsKey(key)) {
            throw new IllegalArgumentException("Key " + key + " not found");
        }
        return localizationMap.get(key);
    }

    @Override
    public void onApplicationEvent(LocalizationChangeEvent event) {
        localizationManager.setLocale(event.getLocale());
        localizationMap.keySet().forEach(key -> localizationMap.get(key).set(localizationManager.get(key)));
    }
}
