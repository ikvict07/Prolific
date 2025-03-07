package org.nevertouchgrass.prolific.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.nevertouchgrass.prolific.annotation.OnDelete;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class OnDeleteAspect extends BasicAspect {
    public OnDeleteAspect(ConfigurableBeanFactory beanFactory, MethodCaller methodCaller) {
        super(OnDelete.class, beanFactory, methodCaller);
    }

    @Override
    @Pointcut("execution(* delete*(..)) && @within(org.springframework.stereotype.Repository)")
    public void getPointcut() {
    }
}
