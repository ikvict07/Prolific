package org.nevertouchgrass.prolific.service.settings;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
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
    private final XmlMapper xmlMapper;
    @Getter
    private Path projectFilesPath;

    private final static String OS_NAME = "os.name";

    @PostConstruct
    public void init() {
        if (System.getProperty(OS_NAME).toLowerCase().contains("win")) {
            projectFilesPath = Paths.get(System.getProperty("user.home") + "/Program Files/Prolific");
        } else {
            projectFilesPath = Paths.get(System.getProperty("user.home"), ".prolific");
        }
        if (!Files.exists(projectFilesPath)) {
            try {
                Files.createDirectories(projectFilesPath);
            } catch (Exception e) {
                log.error("Failed to create project files path", e);
            }
        }
    }

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
        var dir = projectFilesPath.resolve("runConfigs");
        Files.createDirectories(dir);
        return dir;
    }

    @SneakyThrows
    public Path getSettingsPath() {
        Path settingsPath = projectFilesPath;
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
            int startIndex = System.getProperty(OS_NAME).toLowerCase().contains("win") ? 1 : 0;
            String path = fixed.substring(startIndex, endIndex);
            return Paths.get(path);
        }
        if (url.startsWith("file:")) {
            String fixed = url.replace("file:", "");
            int index = fixed.indexOf("/build");
            if (index == -1) {
                throw new NoSuchElementException("Invalid Jar File URL String");
            }
            int startIndex = System.getProperty(OS_NAME).toLowerCase().contains("win") ? 1 : 0;
            String path = fixed.substring(startIndex, index);
            return Paths.get(path);
        }
        return Paths.get(uri);
    }

    @SneakyThrows
    public Path getPluginsPath() {
        Path settingsFilePath = projectFilesPath.resolve("plugins");
        Files.createDirectories(settingsFilePath);
        Path defaultPlugin = settingsFilePath.resolve("defaultPlugin.xml");
        if (!Files.exists(defaultPlugin)) {
            Files.createFile(defaultPlugin);
            Files.write(defaultPlugin, ("""
                    <projects>
                        <project>
                            <name>Gradle</name>
                            <identifiers>
                                <file>build.gradle</file>
                                <file>build.gradle.kts</file>
                                <folder>.gradle</folder>
                            </identifiers>
                        </project>
                    
                        <project>
                            <name>Maven</name>
                            <identifiers>
                                <file>pom.xml</file>
                            </identifiers>
                        </project>
                    
                        <project>
                            <name>Eclipse</name>
                            <identifiers>
                                <file>.project</file>
                                <file>.classpath</file>
                            </identifiers>
                        </project>
                    
                        <project>
                            <name>Python</name>
                            <identifiers>
                                <file>requirements.txt</file>
                                <file>pyproject.toml</file>
                                <file>setup.py</file>
                                <folder>.venv</folder>
                            </identifiers>
                        </project>
                    </projects>
                    """).getBytes());
        }
        return settingsFilePath;
    }
}
