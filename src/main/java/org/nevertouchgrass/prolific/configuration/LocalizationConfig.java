package org.nevertouchgrass.prolific.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocalizationConfig {

    private XMLMessageSource messageSource;

    @Autowired
    public void set(XMLMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Bean
    public MessageSource messageSource() {
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBaseName("messages");
        return messageSource;
    }
}
