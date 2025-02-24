package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class UserSettingsService {
    private final UserSettingsHolder userSettingsHolder;
    private final SpringFXConfigurationProperties configuration;
    private final PathService pathService;


    public UserSettingsService(UserSettingsHolder userSettingsHolder, SpringFXConfigurationProperties springFXConfigurationProperties, PathService pathService) {
        this.userSettingsHolder = userSettingsHolder;
        this.configuration = springFXConfigurationProperties;
        this.pathService = pathService;
    }

    @PostConstruct
    @SneakyThrows
    public void loadSettings() {
        XmlMapper xmlMapper = new XmlMapper();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Path settingsFilePath = getSettingsPath();

        UserSettingsHolder sett = xmlMapper.readValue(Files.newInputStream(settingsFilePath), UserSettingsHolder.class);
        System.out.println(sett);
        if (sett.getBaseScanDirectory() == null || sett.getBaseScanDirectory().isEmpty()) {
            onFirstLoad();
            saveSettings();
        } else {
            userSettingsHolder.load(sett);
        }
        System.out.println(userSettingsHolder);
    }

    @SneakyThrows
    public void saveSettings() {
        System.out.println("saving: " + userSettingsHolder);
        XmlMapper xmlMapper = new XmlMapper();
        Path settingsFilePath = getSettingsPath();
        xmlMapper.writeValue(Files.newOutputStream(settingsFilePath), userSettingsHolder);
    }

    @SneakyThrows
    private Path getSettingsPath() {
        Path jarPath = pathService.getProjectPath();
        Path settingsPath = jarPath.getParent().resolve(configuration.getSettingsLocation());
        Path settingsFilePath = settingsPath.resolve("settings.xml");
        Files.createDirectories(settingsPath);
        System.out.println(settingsFilePath);
        if (!Files.exists(settingsFilePath)) {
            Files.createFile(settingsFilePath);
            XmlMapper xmlMapper = new XmlMapper();
            xmlMapper.writeValue(Files.newOutputStream(settingsFilePath), new UserSettingsHolder());
        }
        return settingsFilePath;
    }


    private void onFirstLoad() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            userSettingsHolder.setBaseScanDirectory("C:\\Users\\#{osUser}\\Documents");
        } else if (os.contains("mac")) {
            userSettingsHolder.setBaseScanDirectory("/Users/#{osUser}/Documents");
        } else if (os.contains("nix") || os.contains("nux")) {
            userSettingsHolder.setBaseScanDirectory("/home/#{osUser}/Documents");
        }
    }
}
