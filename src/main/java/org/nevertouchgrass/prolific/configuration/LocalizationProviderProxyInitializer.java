package org.nevertouchgrass.prolific.configuration;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Proxy;

@Configuration
@RequiredArgsConstructor
public class LocalizationProviderProxyInitializer {

    private final LocalizationProviderInvocationHandler handler;

    @Bean
    public LocalizationProvider afterSingletonsInstantiated() {
        return (LocalizationProvider) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{LocalizationProvider.class},
                handler
        );
    }
}
