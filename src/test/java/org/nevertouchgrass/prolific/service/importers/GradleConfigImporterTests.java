package org.nevertouchgrass.prolific.service.importers;

import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.configurations.importers.GradleConfigImporter;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class GradleConfigImporterTests extends BackendTestBase {
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
