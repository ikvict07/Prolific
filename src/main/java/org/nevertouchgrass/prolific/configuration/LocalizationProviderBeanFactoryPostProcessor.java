package org.nevertouchgrass.prolific.configuration;

import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;

@Component
public class LocalizationProviderBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerSingleton("localizationProvider", Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{LocalizationProvider.class},
                (proxy, method, args) -> {
                    LocalizationProviderInvocationHandler handler = beanFactory.getBean(LocalizationProviderInvocationHandler.class);
                    return handler.invoke(proxy, method, args);
                }
        ));
    }
}
