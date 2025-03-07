package org.nevertouchgrass.prolific.aspect;

import jakarta.annotation.PostConstruct;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

import java.lang.annotation.Annotation;
import java.util.List;

public abstract class BasicAspect {
    @PostConstruct
    public void init() {
        valueExtractor = methodCaller.getValueExtractor(type);
    }

    private final Class<? extends Annotation> type;
    protected final ConfigurableBeanFactory beanFactory;
    protected final MethodCaller methodCaller;

    protected AnnotationValueExtractor<Annotation, Class<?>> valueExtractor;

    protected BasicAspect(Class<? extends Annotation> t, ConfigurableBeanFactory beanFactory, MethodCaller methodCaller) {
        type = t;
        this.beanFactory = beanFactory;
        this.methodCaller = methodCaller;
    }

    @SuppressWarnings("unused")
    public abstract void getPointcut();

    @AfterReturning(pointcut = "getPointcut()", returning = "result")
    public void afterReturningAdvice(Object result) {
        if (result instanceof List<?> list) {
            for (Object entity : list) {
                if (entity != null) {
                    methodCaller.invokeAnnotatedMethods(entity.getClass(), entity, beanFactory, type, valueExtractor);
                }
            }
        } else {
            methodCaller.invokeAnnotatedMethods(result.getClass(), result, beanFactory, type, valueExtractor);
        }

    }

}
