package org.nevertouchgrass.prolific.service.settings;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service that manages paths
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class PathService {
    private final SpringFXConfigurationProperties properties;
    private final XmlMapper xmlMapper;

    public Path getProjectPath() {
        Class<?> clazz = PathService.class;
        URL classResource = clazz.getResource(clazz.getSimpleName() + ".class");
        if (classResource == null) {
            throw new IllegalStateException("class resource is null");
        }
        String url = classResource.toString();

        return normalizeUrl(URI.create(url));
    }

    @SneakyThrows
    public Path getRunConfigsDirectory() {
        var jarPath = getProjectPath();
        var dir = jarPath.getParent().resolve(properties.getRunConfigsLocation());
        Files.createDirectories(dir);
        return dir;
    }

    @SneakyThrows
    public Path getSettingsPath() {
        Path jarPath = getProjectPath();
        Path settingsPath = jarPath.getParent().resolve(properties.getSettingsLocation());
        Path settingsFilePath = settingsPath.resolve("settings.xml");
        Files.createDirectories(settingsPath);
        if (!Files.exists(settingsFilePath)) {
            Files.createFile(settingsFilePath);
            var settings = new UserSettingsHolder();
            xmlMapper.writeValue(Files.newOutputStream(settingsFilePath), settings);
        }
        return settingsFilePath;
    }

    @SneakyThrows
    public List<Path> getWorkspacePaths(Project project) {
        var ideaFolder = Path.of(project.getPath()).resolve(".idea");
        var workspaceFolder = ideaFolder.resolve("workspace.xml");
        var runConfigurationsFolder = ideaFolder.resolve("runConfigurations");
        var result = new ArrayList<Path>();
        if (Files.exists(runConfigurationsFolder)) {
            try (var files = Files.list(runConfigurationsFolder)) {
                result.addAll(files.toList());
            }
        }
        if (Files.exists(workspaceFolder)) {

            result.add(workspaceFolder);
        }
        return result;
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
            return Paths.get(path);
        }
        return Paths.get(uri);
    }
}
