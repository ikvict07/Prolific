package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
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
@RequiredArgsConstructor
public class UserSettingsService {
    private final UserSettingsHolder userSettingsHolder;
    private final PathService pathService;
    private final XmlMapper xmlMapper;

    @PostConstruct
    @SneakyThrows
    public void loadSettings() {
        Path settingsFilePath = pathService.getSettingsPath();
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
        Path settingsFilePath = pathService.getSettingsPath();
        xmlMapper.writeValue(Files.newOutputStream(settingsFilePath), userSettingsHolder);
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
