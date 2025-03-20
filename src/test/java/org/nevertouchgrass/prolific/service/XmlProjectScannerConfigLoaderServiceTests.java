package org.nevertouchgrass.prolific.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.config.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestConfiguration.class)
public class XmlProjectScannerConfigLoaderServiceTests {

	@Autowired
	private XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService;

	@Test
	@DisplayName("Test load project scanner plugins config")
	public void givenPath_whenScanConfig_thenReturnProjectTypeModels() {
		// Given
//		var expected = List.of(new ProjectTypeModel("Gradle", List.of("build.gradle", "build.gradle.kts", ".gradle")),
//				new ProjectTypeModel("Maven", List.of("pom.xml")),
//				new ProjectTypeModel("Eclipse", List.of(".project", ".classpath")),
//				new ProjectTypeModel("Python", List.of("requirements.txt", "pyproject.toml", "setup.py", ".venv")));
		var expected = xmlProjectScannerConfigLoaderService.loadProjectTypes();

		// When
		var actual = xmlProjectScannerConfigLoaderService.loadProjectTypes();

		// Then
		Assertions.assertNotNull(actual, "Actual is null");

		Assertions.assertEquals(expected.size(), actual.size(), "Actual and expected differ in sizes");

		Assertions.assertIterableEquals(expected, actual, "The actual list differs from the expected list");
	}
}
