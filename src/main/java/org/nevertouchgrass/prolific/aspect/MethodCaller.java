package org.nevertouchgrass.prolific.aspect;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

@Component
@Log4j2
public class MethodCaller {

    @SneakyThrows
    @SuppressWarnings("all")
    public void invokeAnnotatedMethods(Class<?> entityClass, Object entity, ConfigurableBeanFactory beanFactory, Class<? extends Annotation> annotationToCall) {
        if (beanFactory instanceof ListableBeanFactory listableBeanFactory) {
            String[] beanNames = listableBeanFactory.getBeanNamesForAnnotation(Component.class);

            for (String beanName : beanNames) {
                Class<?> beanType = listableBeanFactory.getType(beanName);

                if (beanType != null) {
                    for (Method method : beanType.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotationToCall)) {
                            var annotation = method.getAnnotation(annotationToCall);
                            Method valueMethod = annotationToCall.getDeclaredMethod("value");

                            Object value = valueMethod.invoke(annotation);
                            Class<?> targetEntityClass = (Class<?>) value;

                            if (targetEntityClass.isAssignableFrom(entityClass)) {
                                Object bean = null;

                                ConfigurableBeanFactory configurableBeanFactory = beanFactory;
                                if (configurableBeanFactory.containsSingleton(beanName)) {
                                    bean = configurableBeanFactory.getSingleton(beanName);
                                }
                                if (bean != null) {
                                    try {
                                        method.setAccessible(true);
                                        method.invoke(bean, entity);
                                        log.info("Called method {} in bean {} for entity {}",
                                                method.getName(), bean.getClass().getSimpleName(), entityClass.getSimpleName());
                                    } catch (Exception e) {
                                        log.error("Failed to invoke {} method {} in bean {} caused by {}",
                                                method.getName(), annotationToCall, bean.getClass().getSimpleName(), e.getCause());
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
