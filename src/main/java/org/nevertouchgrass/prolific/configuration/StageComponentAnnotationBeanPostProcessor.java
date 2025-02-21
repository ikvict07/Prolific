package org.nevertouchgrass.prolific.configuration;

import javafx.stage.Stage;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class StageComponentAnnotationBeanPostProcessor implements BeanPostProcessor {
	private final ApplicationContext applicationContext;

	public StageComponentAnnotationBeanPostProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) {
		if (bean.getClass().isAnnotationPresent(StageComponent.class)) {
			StageComponent stageComponent = bean.getClass().getAnnotation(StageComponent.class);
			String stageName = stageComponent.value();
			Stage stage = (Stage) applicationContext.getBean(stageName);

			Arrays.stream(bean.getClass().getDeclaredFields()).filter(field -> field.getType() == Stage.class)
					.forEach(field -> {
						field.setAccessible(true);
						try {
							field.set(bean, stage);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					});
		}
		return bean;
	}
}
