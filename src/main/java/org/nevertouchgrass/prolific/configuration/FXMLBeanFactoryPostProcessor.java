package org.nevertouchgrass.prolific.configuration;

import javafx.scene.Parent;
import org.nevertouchgrass.prolific.util.FxmlUtilService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class FXMLBeanFactoryPostProcessor
        implements
        BeanFactoryPostProcessor,
        ApplicationContextAware,
        EnvironmentAware {

    private SpringFXConfigurationProperties projectConfigurationProperties;
    private ApplicationContext applicationContext;

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
            throw new IllegalStateException("BeanFactory is not a BeanDefinitionRegistry!");
        }

        FxmlUtilService.getFxmlNames(projectConfigurationProperties).forEach(fxmlName -> {
            String beanName = fxmlName + "Parent";

            BeanDefinition beanDefinition = BeanDefinitionBuilder
                    .genericBeanDefinition(Parent.class, () -> FxmlUtilService.loadFxml(fxmlName, projectConfigurationProperties, applicationContext))
                    .setScope(BeanDefinition.SCOPE_SINGLETON).setLazyInit(true).getBeanDefinition();

            registry.registerBeanDefinition(beanName, beanDefinition);
        });
    }


    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        BindResult<SpringFXConfigurationProperties> result = Binder.get(environment).bind("project",
                SpringFXConfigurationProperties.class);
        projectConfigurationProperties = result.get();
    }
}
