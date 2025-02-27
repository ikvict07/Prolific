package org.nevertouchgrass.prolific.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.config.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Path;
import java.util.Set;

@Log4j2
@SpringBootTest(classes = TestConfiguration.class)
public class ProjectScannerServiceTests {
    @Autowired
    private ProjectScannerService projectScannerService;

    @Test
    public void givenNothing_whenScanForProjects_returnProjects() {
        String path = System.getProperty("user.dir");
        Set<Path> expected = Set.of(Path.of(path));

        long startTime = System.currentTimeMillis();
        Set<Path> actual = projectScannerService.scanForProjects(path);

        log.info("Path: {}", path);
        log.info("Actual set: {}", actual);
        log.info("Running time: {}", System.currentTimeMillis() - startTime);

        Assertions.assertNotNull(actual);

        Assertions.assertEquals(expected.size(), actual.size(), "Expected and actual differ in sizes");

        Assertions.assertIterableEquals(expected, actual, "The actual set differs from the expected set");
    }
}
