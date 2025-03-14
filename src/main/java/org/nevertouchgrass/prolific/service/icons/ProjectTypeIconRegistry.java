package org.nevertouchgrass.prolific.service.icons;

import javafx.scene.layout.StackPane;
import lombok.AllArgsConstructor;
import lombok.experimental.NonFinal;
import org.nevertouchgrass.prolific.service.icons.contract.ProjectIconFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProjectTypeIconRegistry {
    private final List<ProjectIconFactory> factories;

    public StackPane getConfigTypeIcon(@NonFinal String type) {
        var factory = factories.stream().filter(f -> f.getProjectType().equalsIgnoreCase(type))
                .findFirst();
        if (factory.isPresent()) {
            return factory.get().configure();
        }
        return factories.stream().filter(f -> f.getProjectType().equals("default"))
                .findFirst().get().configure();
    }
}
