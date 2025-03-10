package org.nevertouchgrass.prolific.service.icons;

import javafx.scene.layout.StackPane;
import lombok.AllArgsConstructor;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.icons.contract.ProjectIconFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProjectTypeIconRegistry {
    private final List<ProjectIconFactory> factories;

    public StackPane configure(Project project) {
        var factory = factories.stream().filter(f -> f.getProjectType().equalsIgnoreCase(project.getType()))
                .findFirst();
        if (factory.isPresent()) {
            return factory.get().configure();
        }
        return factories.stream().filter(f -> f.getProjectType().equals("default"))
                .findFirst().get().configure();
    }
}
