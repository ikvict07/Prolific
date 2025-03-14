package org.nevertouchgrass.prolific.service.importers;

import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.config.TestConfiguration;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = TestConfiguration.class)
class GradleConfigImporterTests {

    private final GradleConfigImporter importer;

    @Autowired
    public GradleConfigImporterTests(GradleConfigImporter importer) {
        this.importer = importer;
    }

    @Test
    void shouldResolveRunConfigs() {
        var p = new Project();
        p.setPath(System.getProperty("user.dir"));
        var result = importer.importConfig(p);
        if (Path.of(System.getProperty("user.dir")).resolve(".idea").resolve("workspace.xml").toFile().exists()) {
            assertThat(result).isNotEmpty();
        } else {
            assertThat(result).isEmpty();
        }
    }
}
