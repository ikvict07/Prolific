package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Log4j2
public class UserSettingsService {
    private final UserSettingsHolder userSettingsHolder;
    private final SpringFXConfigurationProperties configuration;
    private final PathService pathService;
    private final XmlMapper xmlMapper;

    private UserSettingsService it;

    @Autowired
    public void setSelf(@Lazy UserSettingsService it) {
        this.it = it;
    }

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
        userSettingsHolder.load(sett);
        log.info("Using settings: {}", userSettingsHolder);
    }

    @SneakyThrows
    @Async
    synchronized public void saveSettings() {
        log.info("Saving settings: " + userSettingsHolder);
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
            xmlMapper.writeValue(Files.newOutputStream(settingsFilePath), new UserSettingsHolder());
        }
        return settingsFilePath;
    }


    public void setDefaultBaseScanDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String osUser = System.getProperty("user.name");
        if (os.contains("win")) {
            userSettingsHolder.setBaseScanDirectory("C:\\Users\\" + osUser + "\\");
        } else if (os.contains("mac")) {
            userSettingsHolder.setBaseScanDirectory("/Users/" + osUser + "/");
        } else if (os.contains("nix") || os.contains("nux")) {
            userSettingsHolder.setBaseScanDirectory("/home/" + osUser + "/");
        }
        it.saveSettings();
    }

    public void setDefaultRescanEvery() {
        userSettingsHolder.setRescanEveryHours(24);
        it.saveSettings();
    }

    public void setDefaultProjects () {
        userSettingsHolder.setUserProjects(List.of());
        it.saveSettings();
    }
    public void setDefaultLastScanDate() {
        userSettingsHolder.setLastScanDate(LocalDateTime.MIN);
        it.saveSettings();
    }
}
