package org.nevertouchgrass.prolific.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Getter
@Setter
public class PluginConfigProvider {

	private Path pluginConfigPath;

	public PluginConfigProvider() {
		this.pluginConfigPath = Path.of("src/main/resources/plugin/plugins.xml");
	}
}
