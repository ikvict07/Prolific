package org.nevertouchgrass.prolific.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

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
        if (url.startsWith("jar:")) {
            String fixed = url
                    .replace("jar:", "")
                    .replace("file:", "")
                    .replace("nested:", "");
            int index = fixed.indexOf(".jar");
            if (index == -1) {
                throw new NoSuchElementException("Invalid Jar File URL String");
            }
            String path;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                 path = fixed.substring(1, index + 4);

            } else {
                 path = fixed.substring(0, index + 4);

            }
            return Paths.get(path);
        }
        if (url.startsWith("file:")) {
            String fixed = url.replace("file:", "");
            int index = fixed.indexOf("/build");
            if (index == -1) {
                throw new NoSuchElementException("Invalid Jar File URL String");
            }
            String path;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                path = fixed.substring(1, index);
            } else {
                path = fixed.substring(0, index);
            }
            log.info("Working in directory: {}", path);
            return Paths.get(path);
        }
        throw new IllegalStateException("Invalid Jar File URL String");
    }
}
