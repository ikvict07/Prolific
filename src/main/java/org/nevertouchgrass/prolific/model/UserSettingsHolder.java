package org.nevertouchgrass.prolific.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@Data
@JacksonXmlRootElement(localName = "settings")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSettingsHolder {
    private String baseScanDirectory;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd['T'HH:mm:ss]")
    private LocalDateTime lastScanDate = LocalDateTime.now().minusYears(100);
    private Integer rescanEveryHours;
    private List<Project> userProjects;
    private Integer maximumProjectDepth = 6;
    private List<String> excludedDirs;
    private List<String> supportedTranslations;
    private Locale locale;
    private String pythonPath;
    private String gradlePath;
    private String mavenPath;
    private String jdkPath;

    public void load(UserSettingsHolder userSettingsHolder) {
        if (userSettingsHolder.getBaseScanDirectory() != null && !userSettingsHolder.getBaseScanDirectory().isEmpty()) {
            this.baseScanDirectory = userSettingsHolder.getBaseScanDirectory();
        }
        if (userSettingsHolder.getLastScanDate() != null) {
            this.lastScanDate = userSettingsHolder.getLastScanDate();
        }
        if (userSettingsHolder.getRescanEveryHours() != null) {
            this.rescanEveryHours = userSettingsHolder.getRescanEveryHours();
        }
        if (userSettingsHolder.getUserProjects() != null) {
            this.userProjects = userSettingsHolder.getUserProjects();
        }
        if (userSettingsHolder.maximumProjectDepth != null) {
            this.maximumProjectDepth = userSettingsHolder.getMaximumProjectDepth();
        }
        if (userSettingsHolder.excludedDirs != null && !userSettingsHolder.excludedDirs.isEmpty()) {
            this.excludedDirs = userSettingsHolder.excludedDirs;
        }
        if (userSettingsHolder.getSupportedTranslations() != null && !userSettingsHolder.getSupportedTranslations().isEmpty()) {
            this.supportedTranslations = userSettingsHolder.supportedTranslations;
        }
        if (userSettingsHolder.getLocale() != null && !userSettingsHolder.getLocale().getLanguage().isEmpty()) {
            this.locale = userSettingsHolder.getLocale();
        }
        if (userSettingsHolder.getPythonPath() != null && !userSettingsHolder.getPythonPath().isEmpty()) {
            this.pythonPath = userSettingsHolder.getPythonPath();
        }
        if (userSettingsHolder.getGradlePath() != null && !userSettingsHolder.getGradlePath().isEmpty()) {
            this.gradlePath = userSettingsHolder.getGradlePath();
        }
        if (userSettingsHolder.getMavenPath() != null && !userSettingsHolder.getMavenPath().isEmpty()) {
            this.mavenPath = userSettingsHolder.getMavenPath();
        }
        if (userSettingsHolder.getJdkPath() != null && !userSettingsHolder.getJdkPath().isEmpty()) {
            this.jdkPath = userSettingsHolder.getJdkPath();
        }
    }
}
