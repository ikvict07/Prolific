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
	private Path pluginConfigPath;

	@SneakyThrows
	public PluginConfigProvider() {
		Path settingsPath = Path.of("src/main/resources/plugin/");
		Path settingsFilePath = settingsPath.resolve("plugins.xml");
		Files.createDirectories(settingsPath);
		if (!Files.exists(settingsFilePath)) {
			Files.createFile(settingsFilePath);
			Files.write(settingsFilePath, "<plugins></plugins>".getBytes());
		}
		this.pluginConfigPath = settingsFilePath;
	}
}
