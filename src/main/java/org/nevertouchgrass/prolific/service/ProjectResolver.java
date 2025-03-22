package org.nevertouchgrass.prolific.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * Service for resolution of project's type
 */
@Service
@DependsOn("userSettingsService")
public class ProjectResolver {
    private List<ProjectTypeModel> projectTypeModels;
    private final UserSettingsHolder userSettingsHolder;

    private PathMatcher excludeMatcher;

    private final XmlProjectScannerConfigLoaderService configLoaderService;

    public ProjectResolver(@NonNull XmlProjectScannerConfigLoaderService configLoaderService, UserSettingsHolder userSettingsHolder) {
        this.configLoaderService = configLoaderService;
        this.userSettingsHolder = userSettingsHolder;

    }

    @PostConstruct
    public void init() {
        this.projectTypeModels = configLoaderService.loadProjectTypes();
        List<String> exclude = userSettingsHolder.getExcludedDirs();
        String excludePattern = String.format("glob:**/{%s}", String.join(",", exclude));
        this.excludeMatcher = FileSystems.getDefault().getPathMatcher(excludePattern);
    }

    @SneakyThrows
    public Project resolveProject(Path path, int depth) {
        for (var projectTypeModel : projectTypeModels) {
            Visitor visitor = new Visitor(projectTypeModel, userSettingsHolder, excludeMatcher, depth);
            Files.walkFileTree(path, visitor);
            if (visitor.getProject().getType() != null) {
                Project project = visitor.getProject();
                project.setPath(path.toAbsolutePath().normalize().toString());
                project.setTitle(path.getFileName().toString());
                return project;
            }
        }
        throw new IllegalStateException("Project type not found for path: " + path);
    }

    public Project resolveProject(Path path) {
        return resolveProject(path, Integer.MAX_VALUE);
    }

}

@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal", "NullableProblems"})
class Visitor extends SimpleFileVisitor<Path> {
    private final ProjectTypeModel projectTypeModel;
    private final Project project = new Project();
    private final PathMatcher pathMatcher;
    private final PathMatcher excludeMatcher;
    private final int maxDepth;

    Visitor(ProjectTypeModel projectTypeModel, UserSettingsHolder userSettingsHolder, PathMatcher excludeMatcher, int maxDepth) {
        this.projectTypeModel = projectTypeModel;
        this.excludeMatcher = excludeMatcher;
        List<String> identifiers = projectTypeModel.getIdentifiers();
        String pattern = String.format("glob:**/{%s}", String.join(",", identifiers));
        pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
        this.maxDepth = maxDepth;
    }

    @Override
    public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
        if (excludeMatcher.matches(file) && !pathMatcher.matches(file)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (Files.isReadable(file) && pathMatcher.matches(file)) {
                project.setType(projectTypeModel.getName());
                return FileVisitResult.SKIP_SIBLINGS;
            }

        if (file.getNameCount() > maxDepth) {
            return FileVisitResult.SKIP_SIBLINGS;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) {
        if (excludeMatcher.matches(dir) && !pathMatcher.matches(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (dir.getNameCount() > maxDepth) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (Files.isReadable(dir)) {
            if (pathMatcher.matches(dir)) {
                project.setType(projectTypeModel.getName());
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
}
