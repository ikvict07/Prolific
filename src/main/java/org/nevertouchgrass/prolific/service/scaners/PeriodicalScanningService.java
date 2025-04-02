package org.nevertouchgrass.prolific.service.scaners;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.events.StageShowEvent;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
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
@RequiredArgsConstructor
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


    public void cancelScanning() {
        log.info("Canceling scanning");
        projectScannerService.cancelScanning();
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
        if (p.equals(Path.of(userSettingsHolder.getBaseScanDirectory()))) {
            log.info("Skipping base scan directory: {}", p);
            return;
        }
        if (p.toAbsolutePath().normalize().toString().equals(System.getProperty("user.home"))) {
            log.info("Skipping user home directory: {}", p);
            return;
        }
        Project project = projectResolver.resolveProject(p, userSettingsHolder.getMaximumProjectDepth());
        projectsRepository.save(project);
    }

    @Override
    public void onApplicationEvent(StageShowEvent event) {
        it.scheduleScanning();
    }
}
