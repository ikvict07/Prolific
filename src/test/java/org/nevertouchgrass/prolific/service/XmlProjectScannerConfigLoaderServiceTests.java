package org.nevertouchgrass.prolific.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.nevertouchgrass.prolific.service.settings.XmlProjectScannerConfigLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link XmlProjectScannerConfigLoaderService} class.
 */
@SpringBootTest(classes = {XmlProjectScannerConfigLoaderService.class, PathService.class})
class XmlProjectScannerConfigLoaderServiceTests extends BackendTestBase {

	@Autowired
	private XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService;

	@MockitoBean
	private XmlMapper xmlMapper;

	@Test
	void should_load_project_scanner_plugins_config() {
		// Given
		var expected = List.of(
				new ProjectTypeModel("Gradle", List.of("build.gradle", "build.gradle.kts", ".gradle")),
				new ProjectTypeModel("Maven", List.of("pom.xml")),
				new ProjectTypeModel("Eclipse", List.of(".project", ".classpath")),
				new ProjectTypeModel("Python", List.of("requirements.txt", "pyproject.toml", "setup.py", ".venv"))
		);

		// When
		var actual = xmlProjectScannerConfigLoaderService.loadProjectTypes();

		// Then
		assertThat(actual)
				.isNotNull()
				.hasSize(expected.size())
				.containsExactlyElementsOf(expected);
	}
}
