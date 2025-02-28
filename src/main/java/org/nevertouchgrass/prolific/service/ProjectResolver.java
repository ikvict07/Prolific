package org.nevertouchgrass.prolific.service;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
public class ProjectResolver {
    private final List<ProjectTypeModel> projectTypeModels;

    public ProjectResolver(@NonNull XmlProjectScannerConfigLoaderService configLoaderService) {
        this.projectTypeModels = configLoaderService.loadProjectTypes();
    }

    @SneakyThrows
    public Project resolveProject(Path path) {
        for (var projectTypeModel : projectTypeModels) {
            Visitor visitor = new Visitor(projectTypeModel);
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
@SuppressWarnings("NullableProblems")
class Visitor extends SimpleFileVisitor<Path> {
    private final ProjectTypeModel projectTypeModel;


    private final Project project = new Project();
    private final PathMatcher pathMatcher;

    Visitor(ProjectTypeModel projectTypeModel) {
        this.projectTypeModel = projectTypeModel;
        List<String> identifiers = projectTypeModel.getIdentifiers();
        String pattern = String.format("glob:**/{%s}", String.join(",", identifiers));
        pathMatcher = FileSystems.getDefault().getPathMatcher(pattern);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (!Files.isReadable(file)) {
            return FileVisitResult.CONTINUE;
        }
        if (pathMatcher.matches(file)) {
            project.setType(projectTypeModel.getName());
            return FileVisitResult.TERMINATE;
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        if (!Files.isReadable(dir)) {
            return FileVisitResult.SKIP_SUBTREE;
        }
        if (pathMatcher.matches(dir)) {
            project.setType(projectTypeModel.getName());
            return FileVisitResult.SKIP_SUBTREE;
        }

        return FileVisitResult.CONTINUE;
    }
    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.SKIP_SUBTREE;
    }
}
