package org.nevertouchgrass.prolific.service;

import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Service for periodical project scanning
 */

@Service
@Log4j2
@SuppressWarnings({"unused", "FieldCanBeLocal", "NullableProblems"})
public class PeriodicalScanningService implements ApplicationListener<ContextRefreshedEvent> {
    private final ProjectScannerService projectScannerService;
    private final UserSettingsHolder userSettingsHolder;
    private final ProjectResolver projectResolver;
    private final UserSettingsService userSettingsService;

    private PeriodicalScanningService it;

    @Autowired
    private void setSelf(@Lazy PeriodicalScanningService it) {
        this.it = it;
    }

    public PeriodicalScanningService(ProjectScannerService projectScannerService, UserSettingsHolder userSettingsHolder, ProjectResolver projectResolver, @Lazy UserSettingsService userSettingsService) {
        this.projectScannerService = projectScannerService;
        this.userSettingsHolder = userSettingsHolder;
        this.projectResolver = projectResolver;
        this.userSettingsService = userSettingsService;
    }

    public void scheduleScanning() {
        LocalDateTime lastScanDate = userSettingsHolder.getLastScanDate();
        int rescanEvery = userSettingsHolder.getRescanEveryHours();
        String baseScanDirectory = userSettingsHolder.getBaseScanDirectory();
        log.info("Last scan date: {}", lastScanDate);
        if (lastScanDate.isBefore(LocalDateTime.now().minus(Duration.ofHours(rescanEvery)))) {
            log.info("Scanning for projects in {}", baseScanDirectory);
            findProjects(baseScanDirectory);
        }
    }

    private void findProjects(String baseScanDirectory) {
        Set<Path> paths = projectScannerService.scanForProjects(baseScanDirectory);
        log.info("Found {} paths", paths.size());
        List<Project> projects = paths.stream().map(projectResolver::resolveProject).toList();
        log.info("Found {} projects", projects.size());
        userSettingsHolder.setUserProjects(projects);
        userSettingsHolder.setLastScanDate(LocalDateTime.now());
        userSettingsService.saveSettings();
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        it.scheduleScanning();
    }

    @Override
    public boolean supportsAsyncExecution() {
        return false;
    }
}
