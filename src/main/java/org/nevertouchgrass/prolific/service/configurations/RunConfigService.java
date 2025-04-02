package org.nevertouchgrass.prolific.service.configurations;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.SpringFXConfigurationProperties;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectRunConfigs;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.RunConfigFileModel;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.nevertouchgrass.prolific.service.configurations.importers.ConfigImporterStrategy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@Data
@Log4j2
public class RunConfigService {
    private final SpringFXConfigurationProperties properties;
    private final PathService pathService;
    private final XmlMapper xmlMapper;
    private final ConfigImporterStrategy configImporter;

    @SneakyThrows
    public List<RunConfig> getRunConfigsFromDir(Project project) {
        var projectPath = project.getPath();
        var runConfigsDirectory = pathService.getRunConfigsDirectory();
        try (var files = Files.list(runConfigsDirectory)) {
            var candidates = files.filter(path -> Arrays.stream(projectPath.split("/")).toList().getLast().contains(getFileNameWithoutExtension(path)));
            var mappedCandidates = candidates.map(f -> {
                try {
                    return xmlMapper.readValue(Files.newInputStream(f), RunConfigFileModel.class);
                } catch (IOException e) {
                    return null;
                }
            }).filter(Objects::nonNull);
            var result = mappedCandidates.filter(config -> config.getProjectPath().equals(projectPath)).findFirst();
            return result.map(RunConfigFileModel::getConfigs).orElse(List.of());
        }
    }

    private String getFileNameWithoutExtension(Path file) {
        var fileName = file.getFileName().toString();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public void saveRunConfigs(Project project, List<RunConfig> configs) {
        var runConfigsDirectory = pathService.getRunConfigsDirectory();
        var projectPath = project.getPath();
        var runConfigFileModel = new RunConfigFileModel(projectPath, configs);
        var runConfigFile = runConfigsDirectory.resolve(project.getTitle() + ".xml");
        try {
            xmlMapper.writeValue(Files.newOutputStream(runConfigFile), runConfigFileModel);
        } catch (IOException e) {
            log.error("Failed to save run configs for project {}", project.getTitle(), e);
        }
    }

    public List<RunConfig> getRunConfigsFromIdea(Project project) {
        return configImporter.getRunConfigs(project);
    }

    public ProjectRunConfigs getAllRunConfigs(Project project) {
        var importedConfigs = getRunConfigsFromIdea(project);
        var manuallyAddedConfigs = getRunConfigsFromDir(project);
        return new ProjectRunConfigs(importedConfigs, manuallyAddedConfigs);
    }

    public void deleteRunConfig(Project project, RunConfig runConfig) {
        var runConfigsDirectory = pathService.getRunConfigsDirectory();
        var runConfigFile = runConfigsDirectory.resolve(project.getTitle() + ".xml");
        try {
            var runConfigFileModel = xmlMapper.readValue(Files.newInputStream(runConfigFile), RunConfigFileModel.class);
            runConfigFileModel.getConfigs().remove(runConfig);
            xmlMapper.writeValue(Files.newOutputStream(runConfigFile), runConfigFileModel);
        } catch (IOException e) {
            log.error("Failed to delete run config {}", runConfig, e);
        }
    }
}
