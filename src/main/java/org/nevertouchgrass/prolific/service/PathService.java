package org.nevertouchgrass.prolific.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

/**
 * Service that manages paths
 */
@Service
@Log4j2
public class PathService {
    public Path getProjectPath() {
        Class<?> clazz = PathService.class;
        URL classResource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (classResource == null) {
            throw new IllegalStateException("class resource is null");
        }
        String url = classResource.toString();
        log.info("Working in directory: {}", url);

        return normalizeUrl(URI.create(url));
    }

    public Path normalizeUrl(URI uri) {
        String url = uri.toString();
        if (url.startsWith("jar:")) {
            String fixed = url
                    .replace("jar:", "")
                    .replace("file:", "")
                    .replace("nested:", "");
            int index = fixed.indexOf(".jar");
            int endIndex = index == -1 ? fixed.length() : index + 4;
            int startIndex = System.getProperty("os.name").toLowerCase().contains("win") ? 1 : 0;
            String path = fixed.substring(startIndex, endIndex);
            return Paths.get(path);
        }
        if (url.startsWith("file:")) {
            String fixed = url.replace("file:", "");
            int index = fixed.indexOf("/build");
            if (index == -1) {
                throw new NoSuchElementException("Invalid Jar File URL String");
            }
            int startIndex = System.getProperty("os.name").toLowerCase().contains("win") ? 1 : 0;
            String path = fixed.substring(startIndex, index);
            log.info("Working in directory: {}", path);
            return Paths.get(path);
        }
        return Paths.get(uri);
    }
}
