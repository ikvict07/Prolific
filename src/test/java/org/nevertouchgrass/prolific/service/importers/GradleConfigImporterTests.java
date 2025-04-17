package org.nevertouchgrass.prolific.service.importers;

import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.annotation.ProlificTestApplication;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.configurations.importers.GradleConfigImporter;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@ProlificTestApplication
class GradleConfigImporterTests extends BackendTestBase {
    private final GradleConfigImporter importer;

    @Autowired
    public GradleConfigImporterTests(GradleConfigImporter importer) {
        this.importer = importer;
    }

    @MockitoBean
    private LocalizationProvider localizationProvider;

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
