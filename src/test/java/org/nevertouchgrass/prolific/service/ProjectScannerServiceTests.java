package org.nevertouchgrass.prolific.service;

import javafx.beans.property.SimpleStringProperty;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.nevertouchgrass.prolific.BackendTestBase;
import org.nevertouchgrass.prolific.annotation.ProlificTestApplication;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.scaners.ProjectScannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.nio.file.Path;
import java.util.Set;


@Log4j2
@ProlificTestApplication
class ProjectScannerServiceTests extends BackendTestBase {
    @Autowired
    private ProjectScannerService projectScannerService;
    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private LocalizationProvider localizationProvider;

    @Test
    @SneakyThrows
    void givenNothing_whenScanForProjects_returnProjects() {
        Mockito.doNothing().when(notificationService).notifyInfo(Mockito.any(InfoNotification.class));
        Mockito.doReturn(new SimpleStringProperty("test")).when(localizationProvider).log_info_scanning_for_projects();
        Mockito.doReturn(new SimpleStringProperty("test")).when(localizationProvider).log_info_scanning_for_projects_finished();
        Mockito.doReturn(new SimpleStringProperty("test")).when(localizationProvider).log_info_scanning_for_projects_cancelled();

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