package org.nevertouchgrass.prolific.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.nevertouchgrass.prolific.annotation.OnUpdate;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class OnUpdateAspect extends BasicAspect {
    @Override
    @Pointcut("execution(* update*(..)) && @within(org.springframework.stereotype.Repository)")
    public void getPointcut() {
    }

    public OnUpdateAspect(ConfigurableBeanFactory beanFactory, MethodCaller methodCaller) {
        super(OnUpdate.class, beanFactory, methodCaller);
    }
}
