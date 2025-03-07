package org.nevertouchgrass.prolific.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.nevertouchgrass.prolific.annotation.OnSave;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@Log4j2
public class OnSaveAspect {

    private final ConfigurableBeanFactory beanFactory;
    private final MethodCaller methodCaller;

    public OnSaveAspect(ConfigurableBeanFactory beanFactory, MethodCaller methodCaller) {
        this.beanFactory = beanFactory;
        this.methodCaller = methodCaller;
    }

    @After(
            value = "execution(* save(..)) && @within(org.springframework.stereotype.Repository)",
            argNames = "joinPoint"
    )
    public void afterSave(JoinPoint joinPoint) {
        Object[] params = joinPoint.getArgs();

        methodCaller.invokeAnnotatedMethods(params[0].getClass(), params[0], beanFactory, OnSave.class);
    }

    @After(
            value = "execution(* saveAll(..)) && @within(org.springframework.stereotype.Repository)",
            argNames = "joinPoint"
    )
    public void afterSaveAll(JoinPoint joinPoint) {
        List<?> result = (List<?>) joinPoint.getArgs()[0];
        for (Object entity : result) {
            if (entity != null) {
                methodCaller.invokeAnnotatedMethods(entity.getClass(), entity, beanFactory, OnSave.class);
            }
        }
    }

}
