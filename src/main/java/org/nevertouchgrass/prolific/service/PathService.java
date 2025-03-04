package org.nevertouchgrass.prolific.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Service that manages paths
 */
@Service
@Log4j2
public class PathService {
    @SneakyThrows
    public Path getProjectPath() {
        Class<?> clazz = PathService.class;
        URI classResource = URI.create(Objects.requireNonNull(clazz.getResource(clazz.getSimpleName() + ".class")).getPath());
        log.info("Working in directory: {}", classResource);
        return normalizeUrl(classResource);
    }
    @SneakyThrows
    public Path normalizeUrl(URI uri) {
        String url = uri.getSchemeSpecificPart();
        var offset =System.getProperty("os.name").toLowerCase().contains("win") ? 1 : 0;
        url = url.substring(offset);
        if (url.contains(".jar")) {
            int index = url.indexOf(".jar");
            String path = url.substring(0, index + 4);
            return Paths.get(path);
        }
        int index = url.indexOf("/build");
        if (index != -1) {
            String path = url.substring(0, index);
            log.info("Working in directory: {}", path);
            return Paths.get(path);
        }
        return Paths.get(url);
    }
}
