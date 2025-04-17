package org.nevertouchgrass.prolific.service.importers;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.configurations.importers.GradleConfigImporter;
import org.nevertouchgrass.prolific.service.parser.DocumentParser;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link GradleConfigImporter} class.
 */
@SpringBootTest(classes = {GradleConfigImporter.class, PathService.class, DocumentParser.class})
class GradleConfigImporterTests extends BackendTestBase {
    private final GradleConfigImporter gradleConfigImporter;

    @Autowired
    public GradleConfigImporterTests(GradleConfigImporter gradleConfigImporter) {
        this.gradleConfigImporter = gradleConfigImporter;
    }

    @MockitoBean
    private XmlMapper xmlMapper;

    @Test
    void should_import_gradle_config_from_project() {
        // Given
        var project = new Project();
        project.setPath(getProjectRootPath());

        // When
        var result = gradleConfigImporter.importConfig(project);

        // Then
        Path workspaceXmlPath = Path.of(getProjectRootPath())
                .resolve(".idea")
                .resolve("workspace.xml");

        if (workspaceXmlPath.toFile().exists()) {
            // If workspace.xml exists, we expect configurations to be imported
            assertThat(result)
                    .as("Run configurations should be imported when workspace.xml exists")
                    .isNotEmpty();
        } else {
            // If workspace.xml doesn't exist, we expect no configurations
            assertThat(result)
                    .as("Run configurations should be empty when workspace.xml doesn't exist")
                    .isEmpty();
        }
    }
}
