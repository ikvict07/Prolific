package org.nevertouchgrass.prolific.service.settings;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        if (sett.getMaximumProjectDepth() == null) {
            setDefaultProjectDepth();
        }
        if (sett.getExcludedDirs() == null) {
            setDefaultExcludedDirs();
        }
        if (sett.getSupportedTranslations() == null) {
            setDefaultSupportedTranslations();
        }
        if (sett.getLocale() == null || sett.getLocale().getLanguage().isEmpty()) {
            setDefaultLocale();
        }
        if (sett.getPythonPath() == null || sett.getPythonPath().isEmpty()) {
            setDefaultPythonPath();
        }
        if (sett.getGradlePath() == null || sett.getGradlePath().isEmpty()) {
            setDefaultGradlePath();
        }
        if (sett.getMavenPath() == null || sett.getMavenPath().isEmpty()) {
            setDefaultMavenPath();
        }
        if (sett.getJdkPath() == null || sett.getJdkPath().isEmpty()) {
            setDefaultJdkPath();
        }
        if (sett.getUser() == null) {
            setDefaultUser();
        }
        if (sett.getAnacondaPath() == null || sett.getAnacondaPath().isEmpty()) {
            setDefaultAnacondaPath();
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

    private void setDefaultUser() {
        userSettingsHolder.setUserRole("common_user");
        saveSettings();
    }

    public void setDefaultBaseScanDirectory() {
        userSettingsHolder.setBaseScanDirectory(System.getProperty("user.home"));
        saveSettings();
    }

    public void setDefaultRescanEvery() {
        userSettingsHolder.setRescanEveryHours(24);
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
        userSettingsHolder.setExcludedDirs(new ArrayList<>(List.of(
                "OneDrive",
                "AppData",
                "miniconda3",
                "cargo",
                ".*"
        )));
        saveSettings();
    }

    public void setDefaultSupportedTranslations() {
        userSettingsHolder.setSupportedTranslations(List.of(
                "en",
                "sk",
                "ru"
        ));
        saveSettings();
    }

    public void setDefaultLocale() {
        userSettingsHolder.setLocale(Locale.forLanguageTag("en"));
        saveSettings();
    }

    public void setDefaultPythonPath() {
        userSettingsHolder.setPythonPath("");
        saveSettings();
    }

    public void setDefaultGradlePath() {
        userSettingsHolder.setGradlePath("");
        saveSettings();
    }

    public void setDefaultMavenPath() {
        userSettingsHolder.setMavenPath("");
        saveSettings();
    }

    public void setDefaultJdkPath() {
        userSettingsHolder.setJdkPath("");
        saveSettings();
    }
    public void setDefaultAnacondaPath() {
        userSettingsHolder.setAnacondaPath("");
        saveSettings();
    }
}
