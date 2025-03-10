package org.nevertouchgrass.prolific.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Getter
@Setter
@Log4j2
public class PluginConfigProvider {
    private Path pluginConfigPath;

    @SneakyThrows
    public PluginConfigProvider() {
        try {
            this.pluginConfigPath = Path.of(getClass().getClassLoader().getResource("/plugin/plugins.xml").toURI());
        } catch (Exception e) {
            log.error("Error while loading plugin configuration", e);
        }
    }
}
