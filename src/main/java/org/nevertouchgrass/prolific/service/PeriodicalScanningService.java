package org.nevertouchgrass.prolific.service;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.events.StageShowEvent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service for periodical project scanning
 */

@Service
@Log4j2
@SuppressWarnings({"unused", "FieldCanBeLocal", "NullableProblems"})
public class PeriodicalScanningService implements ApplicationListener<StageShowEvent> {
    private final ProjectScannerService projectScannerService;
    private final UserSettingsHolder userSettingsHolder;
    private final ProjectResolver projectResolver;
    private final UserSettingsService userSettingsService;
    private final ProjectsRepository projectsRepository;

    private PeriodicalScanningService it;

    @Autowired
    private void setSelf(@Lazy PeriodicalScanningService it) {
        this.it = it;
    }

    public PeriodicalScanningService(ProjectScannerService projectScannerService, UserSettingsHolder userSettingsHolder, ProjectResolver projectResolver, @Lazy UserSettingsService userSettingsService, ProjectsRepository projectsRepository) {
        this.projectScannerService = projectScannerService;
        this.userSettingsHolder = userSettingsHolder;
        this.projectResolver = projectResolver;
        this.userSettingsService = userSettingsService;
        this.projectsRepository = projectsRepository;
    }

    public void scheduleScanning() {
        new Thread(() -> {
            LocalDateTime lastScanDate = userSettingsHolder.getLastScanDate();
            int rescanEvery = userSettingsHolder.getRescanEveryHours();
            log.info("Last scan date: {}", lastScanDate);
            if (lastScanDate.isBefore(LocalDateTime.now().minus(Duration.ofHours(rescanEvery)))) {
                rescan();
            }
        }).start();
    }

    public void rescan() {
        String baseScanDirectory = userSettingsHolder.getBaseScanDirectory();
        log.info("Deleting projects that are not starred and not manually added");
        projectsRepository.deleteWhereIsStarredIsFalseAndIsManuallyAddedIsFalse();
        log.info("Scanning for projects in {}", baseScanDirectory);
        new Thread(() -> findProjects(baseScanDirectory)).start();
        userSettingsHolder.setLastScanDate(LocalDateTime.now());
        userSettingsService.saveSettings();
    }

    private synchronized void findProjects(String baseScanDirectory) {
        projectScannerService.scanForProjects(baseScanDirectory, this::resolveAndSave);
    }

    @SneakyThrows
    private void resolveAndSave(Path path) {
        Path p = path.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS);
        Project project = projectResolver.resolveProject(p);
        projectsRepository.save(project);
    }

    @Override
    public void onApplicationEvent(StageShowEvent event) {
        it.scheduleScanning();
    }
}
