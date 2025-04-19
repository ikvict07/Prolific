package org.nevertouchgrass.prolific.service.configurations.importers;

import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.parser.DocumentParser;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class GradleConfigImporter extends BuildToolConfigImporter {
    public GradleConfigImporter(PathService pathService, DocumentParser documentParser) {
        super(pathService, documentParser);
    }

    @Override
    public String getType() {
        return "Gradle";
    }

    @Override
    protected String getOptionsAttribute() {
        return "taskNames";
    }

    @Override
    public void normalize(RunConfig runConfig) {
        super.normalize(runConfig);
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        runConfig.getCommand().addFirst(isWindows ? "gradlew.bat" : "./gradlew");
        runConfig.getCommand().addLast("--no-daemon");
    }
}
