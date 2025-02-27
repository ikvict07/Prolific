package org.nevertouchgrass.prolific.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.service.PathService;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Getter
@Setter
public class PluginConfigProvider {
	private final SpringFXConfigurationProperties configuration;
	private Path pluginConfigPath;
	private final PathService pathService;

	@SneakyThrows
	public PluginConfigProvider(SpringFXConfigurationProperties configuration, PathService pathService) {
		this.configuration = configuration;
		this.pathService = pathService;
		Path jarPath = pathService.getProjectPath();
		System.out.println("Path: " + jarPath);
		Path settingsPath = jarPath.getParent().resolve(configuration.getSettingsLocation());
		Path settingsFilePath = settingsPath.resolve("plugins.xml");
		Files.createDirectories(settingsPath);
		if (!Files.exists(settingsFilePath)) {
			Files.createFile(settingsFilePath);
			Files.write(settingsFilePath, "<plugins></plugins>".getBytes());
		}
		this.pluginConfigPath = settingsFilePath;
	}
}
