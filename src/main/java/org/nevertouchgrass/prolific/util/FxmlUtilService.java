package org.nevertouchgrass.prolific.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Util class for more convenient fxml operations
 */
public class FxmlUtilService {
    private FxmlUtilService() {
    }

    public static List<String> getFxmlNames(SpringFXConfigurationProperties projectConfigurationProperties) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver
                    .getResources("classpath*:" + projectConfigurationProperties.getFxmlLocation() + "/*.fxml");

            return Arrays.stream(resources).map(Resource::getFilename).filter(Objects::nonNull)
                    .map(name -> name.substring(0, name.length() - 5)).toList();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan FXML files", e);
        }
    }

    public static List<String> getIconNames(SpringFXConfigurationProperties projectConfigurationProperties) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver
                    .getResources("classpath*:" + projectConfigurationProperties.getIconLocation() + "/*.fxml");

            return Arrays.stream(resources).map(Resource::getFilename).filter(Objects::nonNull)
                    .map(name -> name.substring(0, name.length() - 5)).toList();

        } catch (IOException e) {
            throw new IllegalStateException("Failed to scan FXML files", e);
        }
    }

    @SneakyThrows
    public static Parent loadFxml(String fxmlName, SpringFXConfigurationProperties projectConfigurationProperties, ApplicationContext applicationContext) {
        FXMLLoader loader = getFxmlLoader(fxmlName, projectConfigurationProperties, applicationContext);
        loader.setControllerFactory(applicationContext::getBean);
        Parent p = loader.load();
        p.getProperties().put("controller", loader.getController());
        return p;
    }

    @SneakyThrows
    public static Parent loadIcon(String fxmlName, SpringFXConfigurationProperties projectConfigurationProperties, ApplicationContext applicationContext) {
        FXMLLoader loader = getFxmlLoader(fxmlName, projectConfigurationProperties, applicationContext);
        return loader.load();
    }


    public static FXMLLoader getIconLoader(String fxmlName, SpringFXConfigurationProperties projectConfigurationProperties, ApplicationContext applicationContext) {
        String fxmlLocation = projectConfigurationProperties.getIconLocation();
        return new FXMLLoader(
                Objects.requireNonNull(applicationContext.getClass().getResource("/" + fxmlLocation + "/" + fxmlName + ".fxml")));
    }
    @SneakyThrows
    public static FXMLLoader getFxmlLoader(String fxmlName, SpringFXConfigurationProperties projectConfigurationProperties, ApplicationContext applicationContext) {
        String fxmlLocation = projectConfigurationProperties.getFxmlLocation();
        return new FXMLLoader(
                Objects.requireNonNull(applicationContext.getClass().getResource("/" + fxmlLocation + "/" + fxmlName + ".fxml")));
    }
}
