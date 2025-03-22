package org.nevertouchgrass.prolific.service.configurations.importers;

import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.DocumentParser;
import org.nevertouchgrass.prolific.service.PathService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class MavenConfigImporter extends BuildToolConfigImporter {

    public MavenConfigImporter(PathService pathService, DocumentParser documentParser) {
        super(pathService, documentParser);
    }

    @Override
    public String getType() {
        return "Maven";
    }

    @Override
    protected String getOptionsAttribute() {
        return "goals";
    }

    @Override
    public void normalize(RunConfig runConfig) {
        super.normalize(runConfig);
        runConfig.getCommand().addFirst("mvn");
    }

}
