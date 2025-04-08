package org.nevertouchgrass.prolific.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.Set;

@Log4j2
class ProjectScannerServiceTests extends BackendTestBase {
    @Autowired
    private ProjectScannerService projectScannerService;

    @Test
    void givenNothing_whenScanForProjects_returnProjects() {
        String path = System.getProperty("user.dir");
        Set<Path> expected = projectScannerService.scanForProjects(path);

        long startTime = System.currentTimeMillis();
        Set<Path> actual = projectScannerService.scanForProjects(path);
        log.info("Running time: {}", System.currentTimeMillis() - startTime);

        log.info("Path: {}", path);
        log.info("Actual set: {}", actual);
        log.info("Expected set: {}", expected);

        Assertions.assertNotNull(actual);

        Assertions.assertEquals(expected.size(), actual.size(), "Expected and actual differ in sizes");

        Assertions.assertIterableEquals(expected, actual, "The actual set differs from the expected set");
    }
}