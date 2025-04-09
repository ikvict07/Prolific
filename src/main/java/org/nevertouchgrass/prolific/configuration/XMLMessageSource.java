package org.nevertouchgrass.prolific.configuration;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Component
@Slf4j
public class XMLMessageSource extends AbstractMessageSource {
    @Setter(onMethod_ = @Autowired)
    private ResourceLoader resourceLoader;

    private String baseName;
    private final Map<Locale, Properties> messages = new HashMap<>();
    private Properties defaultProperties;

    public void setBaseName(String baseName) {
        this.baseName = baseName;
        defaultProperties = messages.computeIfAbsent(Locale.forLanguageTag("en"), this::computeIfAbsent);
    }

    public Properties getProperties(Locale locale) {
        Properties properties = messages.computeIfAbsent(locale, this::computeIfAbsent);
        return properties == null ? defaultProperties : properties;
    }

    @Override
    protected MessageFormat resolveCode(@NonNull String code, @NonNull Locale locale) {
        Properties properties = messages.computeIfAbsent(locale, this::computeIfAbsent);

        if (properties != null) {
            String message = properties.getProperty(code);
            if (message != null) {
                return new MessageFormat(message, locale);
            }
        }
        return new MessageFormat(defaultProperties.getProperty(code), locale);
    }

    private Properties computeIfAbsent(Locale locale) {
        Properties props = new Properties();
        String localeSuffix = locale.toString().isEmpty() ? "" : "_" + locale;
        String fileName = String.format("%s%s.xml", baseName, localeSuffix);

        try (InputStream inputStream = resourceLoader.getResource(fileName).getInputStream()) {
            props.loadFromXML(inputStream);
        } catch (Exception e) {
            log.error("Failed to load XML file: {}. {}", fileName, e.getMessage());
            return null;
        }
        return props;
    }
}
