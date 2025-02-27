package org.nevertouchgrass.prolific.service;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
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

    @Autowired
    public ProjectScannerService(@NonNull XmlProjectScannerConfigLoaderService configLoaderService) {
        this.projectTypeModels = configLoaderService.loadProjectTypes();
    }

    public Set<Path> scanForProjects(String rootDirectory) {
        try (ForkJoinPool pool = new ForkJoinPool()) {
            return pool.invoke(new FileVisitorTask(new File(rootDirectory).toPath()));
        }
    }

    private class FileVisitorTask extends RecursiveTask<Set<Path>> {
        private final Path path;
        private final PathMatcher pathMatcher;

        private FileVisitorTask(Path path) {
            this.path = path;
            List<String> matchers = new ArrayList<>();
            for (ProjectTypeModel projectTypeModel : projectTypeModels) {
                matchers.addAll(projectTypeModel.getIdentifiers());
            }
            String pattern = String.format("glob:**/{%s}", String.join(",", matchers));
            this.pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        }

        @Override
        protected Set<Path> compute() {
            try {
                List<FileVisitorTask> fileVisitorTasks = new ArrayList<>();
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path,
                        entry -> Files.isReadable(entry) && !Files.isSymbolicLink(entry) && Files.exists(entry)
                )) {
                    for (Path entry : directoryStream) {
                        if (Files.isDirectory(entry)) {
                            if (pathMatcher.matches(entry)) {
                                projects.add(entry.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
                                break;
                            }
                            FileVisitorTask fileVisitorTask = new FileVisitorTask(entry);
                            fileVisitorTask.fork();
                            fileVisitorTasks.add(fileVisitorTask);
                        } else {
                            if (pathMatcher.matches(entry)) {
                                projects.add(entry.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    log.error("{}", e.getMessage());
                }
                for (FileVisitorTask fileVisitorTask : fileVisitorTasks) {
                    projects.addAll(fileVisitorTask.join());
                }
            } catch (Exception e) {
                log.error("{}", e.getMessage());
            }
            return projects;
        }
    }
}
