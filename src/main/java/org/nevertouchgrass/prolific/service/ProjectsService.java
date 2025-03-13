package org.nevertouchgrass.prolific.service;

import jakarta.annotation.PostConstruct;
import org.nevertouchgrass.prolific.annotation.OnDelete;
import org.nevertouchgrass.prolific.annotation.OnSave;
import org.nevertouchgrass.prolific.annotation.OnUpdate;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ProjectsService {

    private final ProjectResolver projectResolver;
    private final ProjectsRepository projectsRepository;
    private final Set<Project> projects = ConcurrentHashMap.newKeySet();

    private final List<Consumer<Project>> onAddListeners = new ArrayList<>();
    private final List<Consumer<Project>> onRemoveListeners = new ArrayList<>();
    private final List<Consumer<Project>> onUpdateListeners = new ArrayList<>();

    public ProjectsService(ProjectResolver projectResolver, ProjectsRepository projectsRepository) {
        this.projectResolver = projectResolver;
        this.projectsRepository = projectsRepository;
    }

    @PostConstruct
    private void init() {
        projectsRepository.findAll(Project.class).forEach(projects::add);
    }

    public Set<Project> getProjects() {
        return Set.copyOf(projects);
    }

    public void manuallyAddProject(Path path) {
        var project = projectResolver.resolveProject(path);
        project.setIsManuallyAdded(true);
        projectsRepository.save(project);
    }

    @OnSave(Project.class)
    public void onSave(Project project) {
        if (projects.stream().map(Project::getId).collect(Collectors.toSet()).contains(project.getId())) {
            return;
        }
        projects.add(project);
        onAddListeners.forEach(listener -> listener.accept(project));
    }

    @OnDelete(Project.class)
    public void onDelete(Project project) {
        if (!projects.stream().map(Project::getId).collect(Collectors.toSet()).contains(project.getId())) {
            return;
        }
        projects.remove(project);
        onRemoveListeners.forEach(listener -> listener.accept(project));
    }

    @OnUpdate(Project.class)
    public void onUpdate(Project project) {
        onUpdateListeners.forEach(listener -> listener.accept(project));
    }

    public void registerOnAddListener(Consumer<Project> listener) {
        onAddListeners.add(listener);
    }

    public void registerOnRemoveListener(Consumer<Project> listener) {
        onRemoveListeners.add(listener);
    }

    public void registerOnUpdateListener(Consumer<Project> listener) {
        onUpdateListeners.add(listener);
    }
}
