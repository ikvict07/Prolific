package org.nevertouchgrass.prolific.service;

import org.springframework.stereotype.Service;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PathService {

    public Path getProjectPath() {
        Class<?> clazz = PathService.class;
        URL classResource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (classResource == null) {
            throw new RuntimeException("class resource is null");
        }
        String url = classResource.toString();
        System.out.println("URL is: " + url);
        if (url.startsWith("jar:")) {
            String fixed = url
                    .replace("jar:", "")
                    .replace("file:", "")
                    .replace("nested:", "");
            int index = fixed.indexOf(".jar");
            if (index == -1) {
                throw new RuntimeException("Invalid Jar File URL String");
            }
            String path = fixed.substring(0, index + 4);
            return Paths.get(path);
        }
        throw new RuntimeException("Invalid Jar File URL String");
    }
}
