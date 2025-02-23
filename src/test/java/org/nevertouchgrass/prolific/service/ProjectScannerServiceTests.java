package org.nevertouchgrass.prolific.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.config.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.nio.file.Path;
import java.util.Set;

@SpringBootTest
@ContextConfiguration(classes = TestConfiguration.class)
public class ProjectScannerServiceTests {
	@Autowired
	private ProjectScannerService projectScannerService;

	@Test
	public void givenNothing_whenScanForProjects_returnProjects() {
		String path = ".";
		Set<Path> expected = Set.of(Path.of(System.getProperty("user.dir")));
		Set<Path> actual = projectScannerService.scanForProjects(path);

		Assertions.assertNotNull(actual, "Actual is null");

		Assertions.assertEquals(expected.size(), actual.size(), "Expected and actual differ in sizes");

		Assertions.assertIterableEquals(expected, actual, "The actual set differs from the expected set");
	}
}
