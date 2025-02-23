package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class UserSettingsService {
    private final UserSettingsHolder userSettingsHolder;
    private final SpringFXConfigurationProperties configuration;


    public UserSettingsService(UserSettingsHolder userSettingsHolder, SpringFXConfigurationProperties springFXConfigurationProperties) {
        this.userSettingsHolder = userSettingsHolder;
        this.configuration = springFXConfigurationProperties;
    }

    @PostConstruct
    @SneakyThrows
    public void loadSettings() {
        XmlMapper xmlMapper = new XmlMapper();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath*:" + configuration.getSettingsLocation() + "/*.xml");
        Resource settings = Arrays.stream(resources).filter(resource -> resource.getFilename().equals("settings.xml")).findFirst().get();
        userSettingsHolder.load(xmlMapper.readValue(settings.getInputStream(), UserSettingsHolder.class));
        System.out.println(userSettingsHolder);
    }
}
