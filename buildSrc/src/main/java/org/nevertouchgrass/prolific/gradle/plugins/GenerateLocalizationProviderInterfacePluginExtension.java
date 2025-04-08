package org.nevertouchgrass.prolific.gradle.plugins;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenerateLocalizationProviderInterfacePluginExtension {
    private String resourceFile = "src/main/resources/messages_en.xml";
    private String packageName = "org.nevertouchgrass.prolific.service.localization";
    private String interfaceName = "LocalizationProvider";
}
