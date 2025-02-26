package org.nevertouchgrass.prolific.service;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Log4j2
public class ProjectScannerService {

    private final List<ProjectTypeModel> projectTypeModels;
    private final Set<Path> projects = new HashSet<>();

    @Autowired
    public ProjectScannerService(@NonNull XmlProjectScannerConfigLoaderService configLoaderService) {
        this.projectTypeModels = configLoaderService.loadProjectTypes();
    }

    public Set<Path> scanForProjects(String rootDirectory) {
        File file = new File(rootDirectory);

        if (file.isDirectory()) {
            try {
                Files.walkFileTree(file.toPath(), new ProjectFinder());
            } catch (Exception e) {
                log.error("Error occurred while trying to access file", e);
            }
        }

        return projects;
    }

    private class ProjectFinder extends SimpleFileVisitor<Path> {

        private final PathMatcher pathMatcher;

        private ProjectFinder() {
            List<String> matchers = new ArrayList<>();
            for (ProjectTypeModel projectTypeModel : projectTypeModels) {
                matchers.addAll(projectTypeModel.getIdentifiers());
            }

            String pattern = String.format("glob:**/{%s}", String.join(",", matchers));

            this.pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        }

        @Override
        @NonNull
        public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
            if (pathMatcher.matches(file)) {
                try {
                    projects.add(file.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
                } catch (IOException e) {
                    log.error(e);
                }
                return FileVisitResult.SKIP_SIBLINGS;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        @NonNull
        public FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) {
            if (pathMatcher.matches(dir)) {
                try {
                    projects.add(dir.getParent().toRealPath(LinkOption.NOFOLLOW_LINKS));
                } catch (IOException e) {
                    log.error(e);
                }
                return FileVisitResult.SKIP_SIBLINGS;
            }

            return FileVisitResult.CONTINUE;
        }
    }
}
