package org.nevertouchgrass.prolific.service;

import jakarta.annotation.PostConstruct;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final UserSettingsHolder userSettingsHolder;
    private final NotificationService notificationService;


    public Set<Path> scanForProjects(String rootDirectory, Consumer<Path> onFind) {
        notificationService.notifyInfo(new InfoNotification("Scanning for projects"));
        notificationService.notifyEvent(new EventNotification(EventNotification.EventType.START_PROJECT_SCAN));
        log.info("Scanning for projects in {}", rootDirectory);
        var result = startSearching(Path.of(rootDirectory), onFind);
        log.info("Scanning finished, found {} projects", result.size());
        notificationService.notifyEvent(new EventNotification(EventNotification.EventType.END_PROJECT_SCAN));
        notificationService.notifyInfo(InfoNotification.of("Scanning finished, found {} projects",result.size()));
        return result;
    }

    public Set<Path> scanForProjects(String rootDirectory) {
        return startSearching(Path.of(rootDirectory), _ -> {
        });
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
        List<ProjectTypeModel> projectTypeModels;
        List<String> matchers = new ArrayList<>();
        projectTypeModels = configLoaderService.loadProjectTypes();
        for (ProjectTypeModel projectTypeModel : projectTypeModels) {
            matchers.addAll(projectTypeModel.getIdentifiers());
        }
        List<String> exclude = userSettingsHolder.getExcludedDirs();
        String excludePattern;
        PathMatcher excludeMatcher = null;
        if (exclude == null) {
            excludeMatcher = p -> false;
        } else {
            excludePattern = String.format("glob:**/{%s}", String.join(",", exclude));
            excludeMatcher = FileSystems.getDefault().getPathMatcher(excludePattern);
        }
        String pattern = String.format("glob:**/{%s}", String.join(",", matchers));
        var pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        try (
                var subDirs = Files.list(root);
                final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        ) {
            PathMatcher finalExcludeMatcher = excludeMatcher;
            subDirs.forEach(subDir -> executor.submit(() -> {
                try {
                    Files.walkFileTree(subDir, new SimpleFileVisitor<>() {
                        @Override
                        @NonNull
                        public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
                            if (finalExcludeMatcher.matches(file) && !pathMatcher.matches(file)) {
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
                            if (finalExcludeMatcher.matches(dir) && !pathMatcher.matches(dir)) {
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
                    throw new UncheckedIOException("Error reading files from directory", e);
                }
            }));
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading subdirectories from root directory", e);
        }
        return projects;
    }
}
