package org.nevertouchgrass.prolific.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.nevertouchgrass.prolific.annotation.OnDelete;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
@Log4j2
public class OnDeleteAspect {

    private final ConfigurableBeanFactory beanFactory;

    public OnDeleteAspect(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @AfterReturning(
            pointcut = "execution(* delete*(..)) && @within(org.springframework.stereotype.Repository)",
            returning = "result"
    )
    public void afterDelete(Object result) {
        if (result instanceof List<?> list) {
            for (Object entity : list) {
                if (entity != null) {
                    invokeAnnotatedMethods(entity.getClass(), entity);
                }
            }
        } else {
            invokeAnnotatedMethods(result.getClass(), result);
        }
    }

    @SuppressWarnings("java:S3011")
    private void invokeAnnotatedMethods(Class<?> entityClass, Object entity) {
        if (beanFactory instanceof ListableBeanFactory listableBeanFactory) {
            String[] beanNames = listableBeanFactory.getBeanNamesForAnnotation(Component.class);

            for (String beanName : beanNames) {
                Class<?> beanType = listableBeanFactory.getType(beanName);

                if (beanType != null) {
                    for (Method method : beanType.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(OnDelete.class)) {
                            Class<?> targetEntityClass = method.getAnnotation(OnDelete.class).value();

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
                                        log.error("Failed to invoke @OnSave method {} in bean {} caused by {}",
                                                method.getName(), bean.getClass().getSimpleName(), e.getCause());
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
