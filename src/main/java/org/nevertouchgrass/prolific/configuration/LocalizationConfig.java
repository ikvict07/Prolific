package org.nevertouchgrass.prolific.configuration;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LocalizationConfig {
    private final XMLMessageSource messageSource;

    @Bean
    public MessageSource messageSource() {
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBaseName("messages");
        return messageSource;
    }

    @Bean
    public LocalizationProvider localizationProvider(LocalizationProvider provider) {
        return provider;
    }
}
