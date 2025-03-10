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
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
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
    public Project resolveProject(Path path) {
        for (var projectTypeModel : projectTypeModels) {
            Visitor visitor = new Visitor(projectTypeModel, userSettingsHolder, excludeMatcher);
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

}

@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal", "NullableProblems"})
class Visitor extends SimpleFileVisitor<Path> {
    private final ProjectTypeModel projectTypeModel;
    private final Project project = new Project();
    private final PathMatcher pathMatcher;
    private final UserSettingsHolder userSettingsHolder;
    private final PathMatcher excludeMatcher;

    Visitor(ProjectTypeModel projectTypeModel, UserSettingsHolder userSettingsHolder, PathMatcher excludeMatcher) {
        this.projectTypeModel = projectTypeModel;
        this.userSettingsHolder = userSettingsHolder;
        this.excludeMatcher = excludeMatcher;
        List<String> identifiers = projectTypeModel.identifiers();
        String pattern = String.format("glob:**/{%s}", String.join(",", identifiers));
        pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
    }

    @Override
    public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
        if (excludeMatcher.matches(file) && !pathMatcher.matches(file)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (Files.isReadable(file)) {
            if (pathMatcher.matches(file)) {
                project.setType(projectTypeModel.name());
                return FileVisitResult.SKIP_SIBLINGS;
            }
        }
        if (file.getNameCount() > userSettingsHolder.getMaximumProjectDepth()) {
            return FileVisitResult.SKIP_SIBLINGS;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, @NonNull BasicFileAttributes attrs) {
        if (excludeMatcher.matches(dir) && !pathMatcher.matches(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (dir.getNameCount() > userSettingsHolder.getMaximumProjectDepth()) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (Files.isReadable(dir)) {
            if (pathMatcher.matches(dir)) {
                project.setType(projectTypeModel.name());
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
