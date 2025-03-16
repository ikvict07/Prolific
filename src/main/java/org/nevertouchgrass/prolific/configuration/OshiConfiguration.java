package org.nevertouchgrass.prolific.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

@Configuration
public class OshiConfiguration {

    @Bean
    public SystemInfo systemInfo() {
        return new SystemInfo();
    }

    @Bean
    public OperatingSystem operatingSystem(SystemInfo systemInfo) {
        return systemInfo.getOperatingSystem();
    }
}
