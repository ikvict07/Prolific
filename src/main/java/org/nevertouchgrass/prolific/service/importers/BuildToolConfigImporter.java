package org.nevertouchgrass.prolific.service.importers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.DocumentParser;
import org.nevertouchgrass.prolific.service.PathService;
import org.nevertouchgrass.prolific.service.importers.contract.ConfigImporter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.util.List;

@RequiredArgsConstructor
@Log4j2
public abstract class BuildToolConfigImporter implements ConfigImporter {
    private final PathService pathService;
    private final DocumentParser documentParser;
    @Override
    public List<RunConfig> importConfig(Project project) {
        var workspacePath = pathService.getWorkspacePath(project);
        if (!workspacePath.toFile().exists()) {
            return List.of();
        }
        try {
            Document document = documentParser.parseXmlDocument(workspacePath);
            NodeList configurations = document.getElementsByTagName("configuration");

            var configs = documentParser.getNamedRunConfigs(configurations, getType(), getOptionsAttribute());
            configs.forEach(this::normalize);
            return configs;
        } catch (Exception e) {
            log.error("Failed to import run configurations for project {}", project.getTitle(), e);
            return List.of();
        }
    }

    protected abstract String getOptionsAttribute();

    public void normalize(RunConfig runConfig) {
        runConfig.setType(getType());
    }
}
