package org.nevertouchgrass.prolific.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "project")
public class SpringFXConfigurationProperties {
	private String fxmlLocation;
	private String settingsLocation;
	private String runConfigsLocation;
	private String iconLocation;
	private String guidesUrl;
}
