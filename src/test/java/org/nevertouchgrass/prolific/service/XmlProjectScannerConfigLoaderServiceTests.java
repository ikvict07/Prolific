package org.nevertouchgrass.prolific.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;

import java.util.List;

public class XmlProjectScannerConfigLoaderServiceTests {

	private final XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService = new XmlProjectScannerConfigLoaderService();

	@ParameterizedTest
	@DisplayName("Test load project scanner plugins config")
	@ValueSource(strings = {"src/test/resources/plugin/plugins.xml"})
	public void givenPath_whenScanConfig_thenReturnProjectTypeModels(String path) {
		// Given
		var expected = List.of(new ProjectTypeModel("Gradle", List.of("build.gradle", "build.gradle.kts", ".gradle")),
				new ProjectTypeModel("Maven", List.of("pom.xml")),
				new ProjectTypeModel("IntelliJ IDEA", List.of("*.iml", ".idea")),
				new ProjectTypeModel("Eclipse", List.of(".project", ".classpath")),
				new ProjectTypeModel("Python", List.of("requirements.txt", "pyproject.toml", "setup.py", ".venv")));

		// When
		var actual = xmlProjectScannerConfigLoaderService.loadProjectTypes(path);

		// Then
		Assertions.assertNotNull(actual, "Actual is null");

		Assertions.assertEquals(expected.size(), actual.size(), "Actual and expected differ in sizes");

		Assertions.assertIterableEquals(expected, actual, "The actual list differs from the expected list");
	}
}
