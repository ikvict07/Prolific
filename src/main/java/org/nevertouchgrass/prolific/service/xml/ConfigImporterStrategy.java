package org.nevertouchgrass.prolific.service.xml;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.xml.contract.ConfigImporter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Data
@Log4j2
public class ConfigImporterStrategy {
    private final List<ConfigImporter> importers;

    public List<RunConfig> getRunConfig(Project project) {
        var importer = importers.stream().filter(i -> i.supports(project)).findFirst();
        if (importer.isEmpty()) {
            log.error("No importer found for project {}", project.getTitle());
            return List.of();
        }
        return importer.get().importConfig(project);
    }
}
