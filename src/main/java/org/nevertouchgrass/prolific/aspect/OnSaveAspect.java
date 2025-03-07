package org.nevertouchgrass.prolific.aspect;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.nevertouchgrass.prolific.annotation.OnSave;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Component
@Log4j2
public class OnSaveAspect {

    private final ConfigurableBeanFactory beanFactory;

    public OnSaveAspect(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @After(
            value = "execution(* save(..)) && @within(org.springframework.stereotype.Repository)",
            argNames = "joinPoint"
    )
    public void afterSave(JoinPoint joinPoint) {
        Object[] params = joinPoint.getArgs();

        invokeAnnotatedMethods(params[0].getClass(), params[0]);
    }

    @After(
            value = "execution(* saveAll(..)) && @within(org.springframework.stereotype.Repository)",
            argNames = "joinPoint"
    )
    public void afterSaveAll(JoinPoint joinPoint) {
        List<?> result = (List<?>) joinPoint.getArgs()[0];
        for (Object entity : result) {
            if (entity != null) {
                invokeAnnotatedMethods(entity.getClass(), entity);
            }
        }
    }

    @SuppressWarnings("java:S3011")
    @SneakyThrows
    private void invokeAnnotatedMethods(Class<?> entityClass, Object entity) {
        var idField = entityClass.getDeclaredField("id");
        idField.setAccessible(true);
        if (idField.get(entity) == null) {
            log.info("Id is null {}", entity);
            return;
        }
        if (beanFactory instanceof ListableBeanFactory listableBeanFactory) {
            String[] beanNames = listableBeanFactory.getBeanNamesForAnnotation(Component.class);

            for (String beanName : beanNames) {
                Class<?> beanType = listableBeanFactory.getType(beanName);

                if (beanType != null) {
                    for (Method method : beanType.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(OnSave.class)) {
                            Class<?> targetEntityClass = method.getAnnotation(OnSave.class).value();

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
