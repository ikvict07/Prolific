package org.nevertouchgrass.prolific.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.annotation.ProlificTestApplication;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.settings.XmlProjectScannerConfigLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

@ProlificTestApplication
class XmlProjectScannerConfigLoaderServiceTests extends BackendTestBase {

	@Autowired
	private XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService;

	@MockitoBean
	private LocalizationProvider localizationProvider;

	@Test
	@DisplayName("Test load project scanner plugins config")
	void givenPath_whenScanConfig_thenReturnProjectTypeModels() {
		// Given
		var expected = List.of(new ProjectTypeModel(
				"Gradle", List.of("build.gradle", "build.gradle.kts", ".gradle")),
				new ProjectTypeModel("Maven", List.of("pom.xml")),
				new ProjectTypeModel("Eclipse", List.of(".project", ".classpath")),
				new ProjectTypeModel("Python", List.of("requirements.txt", "pyproject.toml", "setup.py", ".venv")));

		// When
		var actual = xmlProjectScannerConfigLoaderService.loadProjectTypes();

		// Then
		Assertions.assertNotNull(actual, "Actual is null");

		Assertions.assertEquals(expected.size(), actual.size(), "Actual and expected differ in sizes");

		Assertions.assertIterableEquals(expected, actual, "The actual list differs from the expected list");
	}
}
