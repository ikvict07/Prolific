package org.nevertouchgrass.prolific.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.nevertouchgrass.prolific.annotation.OnDelete;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@Log4j2
public class OnDeleteAspect {

    private final ConfigurableBeanFactory beanFactory;
    private final MethodCaller methodCaller;


    public OnDeleteAspect(ConfigurableBeanFactory beanFactory, MethodCaller methodCaller) {
        this.beanFactory = beanFactory;
        this.methodCaller = methodCaller;
    }

    @AfterReturning(
            pointcut = "execution(* delete*(..)) && @within(org.springframework.stereotype.Repository)",
            returning = "result"
    )
    public void afterDelete(Object result) {
        if (result instanceof List<?> list) {
            for (Object entity : list) {
                if (entity != null) {
                    methodCaller.invokeAnnotatedMethods(entity.getClass(), entity, beanFactory, OnDelete.class);
                }
            }
        } else {
            methodCaller.invokeAnnotatedMethods(result.getClass(), result, beanFactory, OnDelete.class);
        }
    }

}
