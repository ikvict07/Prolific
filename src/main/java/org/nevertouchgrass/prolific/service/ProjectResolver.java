package org.nevertouchgrass.prolific.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.configuration.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
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
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * Service for resolution of project's type
 */
@Service
public class ProjectResolver {
    private final List<ProjectTypeModel> projectTypeModels;
    private final UserSettingsHolder userSettingsHolder;

    public ProjectResolver(@NonNull XmlProjectScannerConfigLoaderService configLoaderService, UserSettingsHolder userSettingsHolder) {
        this.projectTypeModels = configLoaderService.loadProjectTypes();
        this.userSettingsHolder = userSettingsHolder;
    }

    @SneakyThrows
    public Project resolveProject(Path path) {
        for (var projectTypeModel : projectTypeModels) {
            Visitor visitor = new Visitor(projectTypeModel, userSettingsHolder);
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

    Visitor(ProjectTypeModel projectTypeModel, UserSettingsHolder userSettingsHolder) {
        this.projectTypeModel = projectTypeModel;
        this.userSettingsHolder = userSettingsHolder;
        List<String> identifiers = projectTypeModel.getIdentifiers();
        String pattern = String.format("glob:**/{%s}", String.join(",", identifiers));
        pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
    }

    @Override
    public FileVisitResult visitFile(Path file, @NonNull BasicFileAttributes attrs) {
        if (Files.isReadable(file)) {
            if (pathMatcher.matches(file)) {
                project.setType(projectTypeModel.getName());
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
        if (StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(dir.iterator(), Spliterator.ORDERED),
                false
        ).anyMatch(element -> element.startsWith(".") && pathMatcher.matches(element))) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (dir.getNameCount() > userSettingsHolder.getMaximumProjectDepth()) {
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
