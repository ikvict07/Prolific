package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing user's settings
 */

@Service
@Log4j2
public class UserSettingsService {
    private final UserSettingsHolder userSettingsHolder;
    private final SpringFXConfigurationProperties configuration;
    private final PathService pathService;
    private final XmlMapper xmlMapper;


    public UserSettingsService(UserSettingsHolder userSettingsHolder, SpringFXConfigurationProperties springFXConfigurationProperties, PathService pathService, XmlMapper xmlMapper) {
        this.userSettingsHolder = userSettingsHolder;
        this.configuration = springFXConfigurationProperties;
        this.pathService = pathService;
        this.xmlMapper = xmlMapper;
    }

    @PostConstruct
    @SneakyThrows
    public void loadSettings() {
        Path settingsFilePath = getSettingsPath();
        UserSettingsHolder sett = xmlMapper.readValue(Files.newInputStream(settingsFilePath), UserSettingsHolder.class);
        log.info("Loaded settings: {}", sett);
        if (sett.getBaseScanDirectory() == null || sett.getBaseScanDirectory().isEmpty()) {
            setDefaultBaseScanDirectory();
        }
        if (sett.getLastScanDate() == null) {
            setDefaultLastScanDate();
        }
        if (sett.getRescanEveryHours() == null) {
            setDefaultRescanEvery();
        }
        if (sett.getUserProjects() == null) {
            setDefaultProjects();
        }
        if (sett.getMaximumProjectDepth() == null) {
            setDefaultProjectDepth();
        }
        if (sett.getExcludedDirs() == null) {
            setDefaultExcludedDirs();
        }
        userSettingsHolder.load(sett);
        log.info("Using settings: {}", userSettingsHolder);
    }

    @SneakyThrows
    public synchronized void saveSettings() {
        log.info("Saving settings: {}", userSettingsHolder);
        Path settingsFilePath = getSettingsPath();
        xmlMapper.writeValue(Files.newOutputStream(settingsFilePath), userSettingsHolder);
    }

    @SneakyThrows
    private Path getSettingsPath() {
        Path jarPath = pathService.getProjectPath();
        Path settingsPath = jarPath.getParent().resolve(configuration.getSettingsLocation());
        Path settingsFilePath = settingsPath.resolve("settings.xml");
        Files.createDirectories(settingsPath);
        if (!Files.exists(settingsFilePath)) {
            Files.createFile(settingsFilePath);
            var settings = new UserSettingsHolder();
            xmlMapper.writeValue(Files.newOutputStream(settingsFilePath), settings);
        }
        return settingsFilePath;
    }


    public void setDefaultBaseScanDirectory() {
        userSettingsHolder.setBaseScanDirectory(System.getProperty("user.home"));
        saveSettings();
    }

    public void setDefaultRescanEvery() {
        userSettingsHolder.setRescanEveryHours(24);
        saveSettings();
    }

    public void setDefaultProjects() {
        userSettingsHolder.setUserProjects(List.of());
        saveSettings();
    }

    public void setDefaultLastScanDate() {
        userSettingsHolder.setLastScanDate(LocalDateTime.now().minusYears(100));
        saveSettings();
    }

    public void setDefaultProjectDepth() {
        userSettingsHolder.setMaximumProjectDepth(6);
        saveSettings();
    }

    public void setDefaultExcludedDirs() {
        userSettingsHolder.setExcludedDirs(List.of(
                "OneDrive",
                "AppData",
                "miniconda3",
                "cargo",
                ".*"
        ));
        saveSettings();
    }
}
