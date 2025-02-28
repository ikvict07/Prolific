package org.nevertouchgrass.prolific.listener;


import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Calls all method marked by @Initialize
 * @see Initialize
 */
@SuppressWarnings("NullableProblems")
@Component
public class InitializeAnnotationProcessor implements ApplicationListener<StageInitializeEvent> {
    private final ApplicationContext applicationContext;

    public InitializeAnnotationProcessor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageInitializeEvent event) {
        applicationContext.getBeansWithAnnotation(Component.class).forEach((_, bean) -> {
            var methods = bean.getClass().getDeclaredMethods();
            Arrays.stream(methods).filter(method -> method.isAnnotationPresent(Initialize.class)).forEach(method -> {
                method.setAccessible(true);
                try {
                    method.invoke(bean);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }
}
