package org.nevertouchgrass.prolific.service;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

@Service
@Log4j2
@SuppressWarnings({"unused", "FieldCanBeLocal", "NullableProblems"})
public class ProjectScannerService {

    private final List<ProjectTypeModel> projectTypeModels;
    private final Set<Path> projects = ConcurrentHashMap.newKeySet();
    private final PathMatcher pathMatcher;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final UserSettingsHolder userSettingsHolder;


    @Autowired
    public ProjectScannerService(@NonNull XmlProjectScannerConfigLoaderService configLoaderService, UserSettingsHolder userSettingsHolder) {
        this.projectTypeModels = configLoaderService.loadProjectTypes();

        List<String> matchers = new ArrayList<>();
        for (ProjectTypeModel projectTypeModel : projectTypeModels) {
            matchers.addAll(projectTypeModel.getIdentifiers());
        }
        String pattern = String.format("glob:**/{%s}", String.join(",", matchers));
        this.pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        this.userSettingsHolder = userSettingsHolder;
    }

    public Set<Path> scanForProjects(String rootDirectory) {
        startSearching(Path.of(rootDirectory));
        return projects;
    }

    private void addProject(Path path) {
        try {
            projects.add(path.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
        } catch (IOException e) {
            log.error("{}", e.getMessage());
        }
    }

    public void startSearching(Path root) {
        List<String> matchers = new ArrayList<>();
        projectTypeModels.stream().map(ProjectTypeModel::getIdentifiers).forEach(matchers::addAll);
        List<PathMatcher> patterns = matchers.stream().map(f -> {
            String p = String.format("glob:**/{%s}", f);
            return FileSystems.getDefault().getPathMatcher(p);
        }).toList();
        try (var subDirs = Files.list(root)) {
            subDirs.forEach(subDir -> executor.submit(() -> {
                try {
                    Files.walkFileTree(subDir, new SimpleFileVisitor<>() {
                        @Override
                        @NonNull
                        public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
                            if (Files.isReadable(file)) {
                                if (pathMatcher.matches(file)) {
                                    addProject(file);
                                    return FileVisitResult.SKIP_SIBLINGS;
                                }
                            }
                            if (file.getNameCount() > userSettingsHolder.getMaximumProjectDepth()) {
                                return FileVisitResult.SKIP_SIBLINGS;
                            }
                            System.out.println("Indexing: " + file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        @NonNull
                        public FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) {
                            if (StreamSupport.stream(
                                    Spliterators.spliteratorUnknownSize(dir.iterator(), Spliterator.ORDERED),
                                    false
                            ).anyMatch(element -> element.startsWith(".") && patterns.stream().noneMatch(p -> p.matches(element)))) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            if (dir.getFileName().endsWith("AppData")) {
                                System.out.println("Skipping AppData");
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            if (dir.getFileName().endsWith("OneDrive")) {
                                System.out.println("Skipping OneDrive");
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            if (dir.getFileName().endsWith("miniconda3")) {
                                System.out.println("Skipping OneDrive");
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            if (dir.getNameCount() > userSettingsHolder.getMaximumProjectDepth()) {
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            if (Files.isReadable(dir)) {
                                if (pathMatcher.matches(dir)) {
                                    addProject(dir);
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        @Override
                        public FileVisitResult visitFileFailed(Path file, IOException exc) {
                            System.out.println("Error reading file: " + file);
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
        executor.shutdown();
        try {
            var _ = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Indexing interrupted", e);
        }
    }
}
