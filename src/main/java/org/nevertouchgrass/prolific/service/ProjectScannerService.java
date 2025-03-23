package org.nevertouchgrass.prolific.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.nevertouchgrass.prolific.model.notification.EventNotification;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Service that scans directories and finds projects
 */

@Service
@Log4j2
@SuppressWarnings({"unused", "FieldCanBeLocal", "NullableProblems"})
@DependsOn("userSettingsService")
@RequiredArgsConstructor
public class ProjectScannerService {

    private final XmlProjectScannerConfigLoaderService configLoaderService;
    private PathMatcher pathMatcher;
    private PathMatcher excludeMatcher;
    private final UserSettingsHolder userSettingsHolder;
    private final NotificationService notificationService;

    private ExecutorService executor;
    private final AtomicBoolean isScanningCancelled = new AtomicBoolean(false);
    private boolean isScanning = false;

    /**
     * Scan for projects in the given root directory
     *
     * @param rootDirectory directory to scan
     * @param onFind        callback for each found project
     * @return Set of found project paths
     * @throws IllegalStateException if scanning is already in progress
     */
    public Set<Path> scanForProjects(String rootDirectory, Consumer<Path> onFind) {
        List<ProjectTypeModel> projectTypeModels;
        List<String> matchers = new ArrayList<>();
        projectTypeModels = configLoaderService.loadProjectTypes();
        for (ProjectTypeModel projectTypeModel : projectTypeModels) {
            matchers.addAll(projectTypeModel.getIdentifiers());
        }
        List<String> exclude = userSettingsHolder.getExcludedDirs();
        if (exclude == null || exclude.isEmpty()) {
            this.excludeMatcher = FileSystems.getDefault().getPathMatcher("glob:**/{.git,.svn,.hg,.bzr}");
        } else {
            String excludePattern = String.format("glob:**/{%s}", String.join(",", exclude));
            this.excludeMatcher = FileSystems.getDefault().getPathMatcher(excludePattern);
        }
        if (matchers.isEmpty()) {
            log.warn("No project types configured. Scanning will not be performed");
            return Set.of();
        } else {
            String pattern = String.format("glob:**/{%s}", String.join(",", matchers));
            this.pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        }
        if (isScanning) {
            throw new IllegalStateException("Scanning is already in progress");
        }

        isScanning = true;
        isScanningCancelled.set(false);

        notificationService.notifyInfo(new InfoNotification("Scanning for projects"));
        notificationService.notifyEvent(new EventNotification(EventNotification.EventType.START_PROJECT_SCAN));
        log.info("Scanning for projects in {}", rootDirectory);

        Set<Path> result;
        try {
            result = startSearching(Path.of(rootDirectory), onFind);
        } finally {
            isScanning = false;
        }

        if (isScanningCancelled.get()) {
            log.info("Scanning was cancelled. Found {} projects so far", result.size());
            notificationService.notifyInfo(InfoNotification.of("Scanning cancelled. Found {} projects", result.size()));
        } else {
            log.info("Scanning finished, found {} projects", result.size());
            notificationService.notifyInfo(InfoNotification.of("Scanning finished, found {} projects", result.size()));
        }

        notificationService.notifyEvent(new EventNotification(EventNotification.EventType.END_PROJECT_SCAN));
        return result;
    }

    /**
     * Scan for projects in the given root directory
     *
     * @param rootDirectory directory to scan
     * @return Set of found project paths
     * @throws IllegalStateException if scanning is already in progress
     */
    public Set<Path> scanForProjects(String rootDirectory) {
        return scanForProjects(rootDirectory, _ -> {
        });
    }

    /**
     * Cancel the current scanning operation.
     * Has no effect if no scanning is in progress.
     *
     * @return true if scanning was cancelled, false if no scanning was in progress
     */
    public boolean cancelScanning() {
        if (!isScanning) {
            return false;
        }

        isScanningCancelled.set(true);

        // Shutdown executor service to stop scanning
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

        notificationService.notifyInfo(new InfoNotification("Scanning cancelled"));
        log.info("Scanning cancelled by user");
        return true;
    }

    /**
     * Check if scanning is currently in progress
     *
     * @return true if scanning is in progress, false otherwise
     */
    public boolean isScanning() {
        return isScanning;
    }

    private void addProject(Set<Path> where, Path path) {
        try {
            where.add(path.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }
    }

    public Set<Path> startSearching(Path root, Consumer<Path> onFind) {
        final Set<Path> projects = ConcurrentHashMap.newKeySet();
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            var subDirs = Files.list(root).toList();
            executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            List<Future<?>> futures = new ArrayList<>();

            for (Path subDir : subDirs) {
                if (isScanningCancelled.get()) {
                    break;
                }

                futures.add(executor.submit(() -> {
                    try {
                        Files.walkFileTree(subDir, new SimpleFileVisitor<>() {
                            @Override
                            @NonNull
                            public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
                                // Check if scanning was cancelled
                                if (isScanningCancelled.get()) {
                                    return FileVisitResult.TERMINATE;
                                }

                                if (excludeMatcher.matches(file) && !pathMatcher.matches(file)) {
                                    return FileVisitResult.CONTINUE;
                                }
                                if (Files.isReadable(file) && pathMatcher.matches(file)) {
                                    onFind.accept(file);
                                    addProject(projects, file);
                                    return FileVisitResult.SKIP_SIBLINGS;
                                }

                                if (file.getNameCount() > userSettingsHolder.getMaximumProjectDepth()) {
                                    return FileVisitResult.SKIP_SIBLINGS;
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            @NonNull
                            public FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) {
                                // Check if scanning was cancelled
                                if (isScanningCancelled.get()) {
                                    return FileVisitResult.TERMINATE;
                                }

                                if (excludeMatcher.matches(dir) && !pathMatcher.matches(dir)) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                if (dir.getNameCount() > userSettingsHolder.getMaximumProjectDepth()) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                if (Files.isReadable(dir)) {
                                    if (pathMatcher.matches(dir)) {
                                        onFind.accept(dir);
                                        addProject(projects, dir);
                                        return FileVisitResult.SKIP_SUBTREE;
                                    }
                                    return FileVisitResult.CONTINUE;
                                }

                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                                if (exc instanceof AccessDeniedException) {
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    } catch (IOException e) {
                        if (!isScanningCancelled.get()) {
                            log.error("Error reading files from directory: {}", e.getMessage());
                        }
                    }
                }));
            }

            for (Future<?> future : futures) {
                try {
                    if (!isScanningCancelled.get()) {
                        future.get();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.debug("Scanning interrupted", e);
                } catch (ExecutionException e) {
                    log.error("Error during scanning", e.getCause());
                } catch (CancellationException e) {
                    // Task was cancelled, which is expected when cancelling scan
                    log.debug("Task was cancelled", e);
                }
            }

        } catch (IOException e) {
            if (!isScanningCancelled.get()) {
                throw new UncheckedIOException("Error reading subdirectories from root directory", e);
            }
        } finally {
            // Always ensure the executor is shutdown
            if (executor != null && !executor.isShutdown()) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    executor.shutdownNow();
                }
            }
        }

        return projects;
    }
}