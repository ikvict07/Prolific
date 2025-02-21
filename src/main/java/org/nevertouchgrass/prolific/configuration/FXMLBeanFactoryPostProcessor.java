package org.nevertouchgrass.prolific.configuration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FXMLBeanFactoryPostProcessor
		implements
			BeanFactoryPostProcessor,
			ApplicationContextAware,
			EnvironmentAware {

	private SpringFXConfigurationProperties projectConfigurationProperties;
	private ApplicationContext applicationContext;

	@Override
	public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
		if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
			throw new IllegalStateException("BeanFactory is not a BeanDefinitionRegistry!");
		}

		getFxmlNames().forEach(fxmlName -> {
			String beanName = fxmlName + "Parent";

			BeanDefinition beanDefinition = BeanDefinitionBuilder
					.genericBeanDefinition(Parent.class, () -> loadFxml(fxmlName))
					.setScope(BeanDefinition.SCOPE_SINGLETON).setLazyInit(true).getBeanDefinition();

			registry.registerBeanDefinition(beanName, beanDefinition);
		});
	}

	@SneakyThrows
	private Parent loadFxml(String fxmlName) {

		String fxmlLocation = projectConfigurationProperties.getFxmlLocation();
		FXMLLoader loader = new FXMLLoader(
				Objects.requireNonNull(getClass().getResource("/" + fxmlLocation + "/" + fxmlName + ".fxml")));
		loader.setControllerFactory(applicationContext::getBean);
		return loader.load();

	}

	private List<String> getFxmlNames() {
		try {
			PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver
					.getResources("classpath*:" + projectConfigurationProperties.getFxmlLocation() + "/*.fxml");

			return Arrays.stream(resources).map(Resource::getFilename).filter(Objects::nonNull)
					.map(name -> name.substring(0, name.length() - 5)).collect(Collectors.toList());

		} catch (IOException e) {
			throw new RuntimeException("Failed to scan FXML files", e);
		}
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setEnvironment(@NonNull Environment environment) {
		BindResult<SpringFXConfigurationProperties> result = Binder.get(environment).bind("project",
				SpringFXConfigurationProperties.class);
		projectConfigurationProperties = result.get();
	}
}
