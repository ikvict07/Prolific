package org.nevertouchgrass.prolific.service;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

@Service
@Log4j2
public class ProjectScannerService {

    private final List<ProjectTypeModel> projectTypeModels;
    private final Set<Path> projects = ConcurrentHashMap.newKeySet();
    private final PathMatcher pathMatcher;

    @Autowired
    public ProjectScannerService(@NonNull XmlProjectScannerConfigLoaderService configLoaderService) {
        this.projectTypeModels = configLoaderService.loadProjectTypes();

        List<String> matchers = new ArrayList<>();
        for (ProjectTypeModel projectTypeModel : projectTypeModels) {
            matchers.addAll(projectTypeModel.getIdentifiers());
        }
        String pattern = String.format("glob:**/{%s}", String.join(",", matchers));
        this.pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
    }

    public Set<Path> scanForProjects(String rootDirectory) {
        try (ForkJoinPool pool = new ForkJoinPool()) {
            return pool.invoke(new FileVisitorTask(new File(rootDirectory).toPath()));
        }
    }

    private class FileVisitorTask extends RecursiveTask<Set<Path>> {
        private final Path path;

        private FileVisitorTask(Path path) {
            this.path = path;
        }

        @Override
        protected Set<Path> compute() {
            try {
                List<FileVisitorTask> fileVisitorTasks = new ArrayList<>();

                Files.walkFileTree(path, new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        if (Files.isReadable(dir)) {
                            if (pathMatcher.matches(dir)) {
                                addProject(dir);
                                return FileVisitResult.SKIP_SUBTREE;
                            }

                            if (!dir.equals(path)) {
                                FileVisitorTask fileVisitorTask = new FileVisitorTask(dir);
                                fileVisitorTask.fork();
                                fileVisitorTasks.add(fileVisitorTask);
                                return FileVisitResult.SKIP_SUBTREE;
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if(Files.isReadable(file)) {
                            if (pathMatcher.matches(file)) {
                                addProject(file);
                                return FileVisitResult.SKIP_SIBLINGS;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        for (FileVisitorTask fileVisitorTask : fileVisitorTasks) {
                            projects.addAll(fileVisitorTask.join());
                        }
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    private void addProject(Path path) {
                        try {
                            projects.add(path.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
                        } catch (IOException e) {
                            log.error("{}", e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            }
            return projects;
        }
    }
}
