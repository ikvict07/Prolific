package org.nevertouchgrass.prolific.configuration;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Data
@JacksonXmlRootElement(localName = "settings")
public class UserSettingsHolder {
    private String baseScanDirectory;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd['T'HH:mm:ss]")
    private LocalDateTime lastScanDate = LocalDateTime.now().minusYears(100);
    private Integer rescanEveryHours;
    private List<Project> userProjects;
    private Integer maximumProjectDepth;

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
    }
}
