package org.nevertouchgrass.prolific.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class LocalizationProviderBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    // Will create a custom InvocationHandler and use it to create BeanDefinition for LocalizationProvider
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }
}
