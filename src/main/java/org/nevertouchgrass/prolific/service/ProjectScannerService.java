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
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
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
public class ProjectScannerService {

    private final XmlProjectScannerConfigLoaderService configLoaderService;
    private PathMatcher pathMatcher;
    private PathMatcher excludeMatcher;
    private final UserSettingsHolder userSettingsHolder;


    @Autowired
    public ProjectScannerService(@NonNull XmlProjectScannerConfigLoaderService configLoaderService, UserSettingsHolder userSettingsHolder) {
        this.configLoaderService = configLoaderService;
        this.userSettingsHolder = userSettingsHolder;
    }

    @PostConstruct
    public void init() {
        List<ProjectTypeModel> projectTypeModels;
        List<String> matchers = new ArrayList<>();
        projectTypeModels = configLoaderService.loadProjectTypes();
        for (ProjectTypeModel projectTypeModel : projectTypeModels) {
            matchers.addAll(projectTypeModel.getIdentifiers());
        }
        List<String> exclude = userSettingsHolder.getExcludedDirs();
        String excludePattern = String.format("glob:**/{%s}", String.join(",", exclude));
        String pattern = String.format("glob:**/{%s}", String.join(",", matchers));
        this.pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        this.excludeMatcher = FileSystems.getDefault().getPathMatcher(excludePattern);
    }

    public Set<Path> scanForProjects(String rootDirectory, Consumer<Path> onFind) {
        var result = startSearching(Path.of(rootDirectory), onFind);
        log.info("Scanning finished");
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

        try (
                var subDirs = Files.list(root);
                final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
        ) {
            subDirs.forEach(subDir -> executor.submit(() -> {
                try {
                    Files.walkFileTree(subDir, new SimpleFileVisitor<>() {
                        @Override
                        @NonNull
                        public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
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
                    throw new UncheckedIOException("Error reading files from directory", e);
                }
            }));
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading subdirectories from root directory", e);
        }
        return projects;
    }
}
