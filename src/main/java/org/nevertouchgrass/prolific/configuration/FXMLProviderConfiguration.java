package org.nevertouchgrass.prolific.configuration;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.model.FxmlLoadedResource;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.util.FxmlUtilService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileNotFoundException;
import java.util.List;

@Configuration
public class FXMLProviderConfiguration {

    private final SpringFXConfigurationProperties configurationProperties;
    private final ApplicationContext context;

    public FXMLProviderConfiguration(SpringFXConfigurationProperties configurationProperties, ApplicationContext context) {
        this.configurationProperties = configurationProperties;
        this.context = context;
    }

    @Bean
    public FxmlProvider fxmlProvider() {
        return this::load;
    }

    @SneakyThrows
    private <T> FxmlLoadedResource<T> load(String parentName) {
        List<String> xmls = FxmlUtilService.getFxmlNames(configurationProperties);
        if (xmls.contains(parentName)) {
            FXMLLoader loader = FxmlUtilService.getFxmlLoader(parentName, configurationProperties, context);
            loader.setControllerFactory(context::getBean);
            Parent parent = loader.load();
            parent.getProperties().put("controller", loader.getController());
            T controller = loader.getController();
            return new FxmlLoadedResource<>(parent, controller);
        }
        throw new FileNotFoundException("Couldn't find " + parentName);
    }
}
