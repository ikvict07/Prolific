package org.nevertouchgrass.prolific.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.nevertouchgrass.prolific.annotation.OnSave;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Log4j2
public class OnSaveAspect extends BasicAspect {
    @Override
    @Pointcut("execution(* save*(..)) && @within(org.springframework.stereotype.Repository)")
    public void getPointcut() {
    }

    public OnSaveAspect(ConfigurableBeanFactory beanFactory, MethodCaller methodCaller) {
        super(OnSave.class, beanFactory, methodCaller);
    }

}
