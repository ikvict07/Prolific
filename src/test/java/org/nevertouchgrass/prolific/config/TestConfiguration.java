package org.nevertouchgrass.prolific.config;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.nevertouchgrass.prolific.configuration.PluginConfigProvider;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.DocumentParser;
import org.nevertouchgrass.prolific.service.PathService;
import org.nevertouchgrass.prolific.service.ProjectScannerService;
import org.nevertouchgrass.prolific.service.XmlProjectScannerConfigLoaderService;
import org.nevertouchgrass.prolific.service.importers.GradleConfigImporter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(SpringFXConfigurationProperties.class)
public class TestConfiguration {
    @Bean
    public XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService(
            PluginConfigProvider pluginConfigProvider) {
        return new XmlProjectScannerConfigLoaderService(pluginConfigProvider);
    }

    @Bean
    public ProjectScannerService projectScannerService(
            XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService, UserSettingsHolder userSettings) {
        System.out.println(xmlProjectScannerConfigLoaderService.loadProjectTypes());
        return new ProjectScannerService(xmlProjectScannerConfigLoaderService, userSettings);
    }

    @Bean
    public UserSettingsHolder userSettingsHolder() {
        var userSettings = new UserSettingsHolder();
        userSettings.setMaximumProjectDepth(5);
        userSettings.setExcludedDirs(List.of("build", "target", ".git", ".idea", ".gradle"));
        return userSettings;
    }

    @Bean
    public PathService pathService(SpringFXConfigurationProperties properties, XmlMapper xmlMapper) {
        return new PathService(properties, xmlMapper);
    }

    @Bean
    public XmlMapper xmlMapper() {
        return new XmlMapper();
    }

    @Bean
    public DocumentParser documentParser() {
        return new DocumentParser();
    }

    @Bean
    public GradleConfigImporter gradleConfigImporter(PathService pathService, DocumentParser documentParser) {
        return new GradleConfigImporter(pathService, documentParser);
    }
}
