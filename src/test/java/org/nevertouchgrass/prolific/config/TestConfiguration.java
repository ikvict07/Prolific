package org.nevertouchgrass.prolific.config;

import org.nevertouchgrass.prolific.configuration.PluginConfigProvider;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.service.PathService;
import org.nevertouchgrass.prolific.service.ProjectScannerService;
import org.nevertouchgrass.prolific.service.XmlProjectScannerConfigLoaderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
public class TestConfiguration {
	@Bean
	public XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService(
			PluginConfigProvider pluginConfigProvider) {
		return new XmlProjectScannerConfigLoaderService(pluginConfigProvider);
	}

	@Bean
	public PluginConfigProvider pluginConfigProvider(SpringFXConfigurationProperties springFXConfigurationProperties, PathService pathService) {
		PluginConfigProvider pluginConfigProvider = new PluginConfigProvider(springFXConfigurationProperties, pathService);
		pluginConfigProvider.setPluginConfigPath(Path.of("src/test/resources/plugin/plugins.xml"));
		return pluginConfigProvider;
	}

	@Bean
	public ProjectScannerService projectScannerService(
			XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService) {
		return new ProjectScannerService(xmlProjectScannerConfigLoaderService);
	}
}
