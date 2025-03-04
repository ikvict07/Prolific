package org.nevertouchgrass.prolific.service;

import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class ProjectsService {

    private final ProjectResolver projectResolver;
    private final ProjectsRepository projectsRepository;

    public ProjectsService(ProjectResolver projectResolver, ProjectsRepository projectsRepository) {
        this.projectResolver = projectResolver;
        this.projectsRepository = projectsRepository;
    }

    public void manuallyAddProject(Path path) {
        var project = projectResolver.resolveProject(path);
        project.setIsManuallyAdded(true);
        projectsRepository.save(project);
    }
}
