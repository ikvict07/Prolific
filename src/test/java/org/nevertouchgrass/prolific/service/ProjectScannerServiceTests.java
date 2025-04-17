package org.nevertouchgrass.prolific.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.scaners.ProjectScannerService;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
import org.nevertouchgrass.prolific.service.settings.XmlProjectScannerConfigLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProjectScannerService} class.
 */
@Log4j2
@SpringBootTest(classes = {ProjectScannerService.class, UserSettingsService.class})
class ProjectScannerServiceTests extends BackendTestBase {
    @Autowired
    private ProjectScannerService projectScannerService;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private LocalizationProvider localizationProvider;

    @MockitoBean
    private UserSettingsHolder userSettingsHolder;

    @MockitoBean
    private XmlProjectScannerConfigLoaderService xmlProjectScannerConfigLoaderService;

    @MockitoBean
    private UserSettingsService userSettingsService;

    @Test
    @SneakyThrows
    void should_scan_for_projects_and_return_consistent_results() {
        // Given
        String path = getProjectRootPath();

        // When - First scan to get expected results
        Set<Path> expected = projectScannerService.scanForProjects(path);

        // When - Second scan to measure performance and get actual results
        long startTime = System.currentTimeMillis();
        Set<Path> actual = projectScannerService.scanForProjects(path);
        long duration = System.currentTimeMillis() - startTime;

        // Log results for debugging
        log.info("Scan completed in {} ms", duration);
        log.info("Path: {}", path);
        log.info("Found {} projects", actual.size());

        // Then
        assertThat(actual)
                .isNotNull()
                .hasSize(expected.size())
                .containsExactlyInAnyOrderElementsOf(expected);
    }
}
