package org.nevertouchgrass.prolific.aspect;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.OnAction;
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
    public void invokeAnnotatedMethods(Class<?> entityClass, Object entity, ConfigurableBeanFactory beanFactory, Class<? extends Annotation> annotationToCall, AnnotationValueExtractor<Annotation, Class<?>> valueExtractor) {
        if (beanFactory instanceof ListableBeanFactory listableBeanFactory) {
            String[] beanNames = listableBeanFactory.getBeanNamesForAnnotation(Component.class);

            for (String beanName : beanNames) {
                Class<?> beanType = listableBeanFactory.getType(beanName);

                if (beanType != null) {
                    for (Method method : beanType.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(annotationToCall)) {
                            Class<?> value = valueExtractor.extract(method.getAnnotation(annotationToCall));


                            if (value.isAssignableFrom(entityClass)) {
                                Object bean;

                                if (beanFactory.containsSingleton(beanName)) {
                                    bean = beanFactory.getSingleton(beanName);
                                } else {
                                    bean = null;
                                }
                                if (bean != null) {
                                    new Thread(() -> {
                                        try {
                                            method.setAccessible(true);
                                            method.invoke(bean, entity);
                                            log.info("Called method {} in bean {} for entity {}",
                                                    method.getName(), bean.getClass().getSimpleName(), entityClass.getSimpleName());
                                        } catch (Exception e) {
                                            log.error("Failed to invoke {} method {} in bean {} caused by {}",
                                                    method.getName(), annotationToCall, bean.getClass().getSimpleName(), e.getCause());
                                        }
                                    }).start();
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    public AnnotationValueExtractor<Annotation, Class<?>> getValueExtractor(Class<? extends Annotation> annotation) {
        return annotation.getAnnotation(OnAction.class).value().extractor;
    }
}
