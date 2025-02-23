package org.nevertouchgrass.prolific.configuration;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
@JacksonXmlRootElement(localName = "settings")
public class UserSettingsHolder {
    private String baseScanDirectory;

    public void load(UserSettingsHolder userSettingsHolder) {
        this.baseScanDirectory = userSettingsHolder.getBaseScanDirectory();
    }
}
