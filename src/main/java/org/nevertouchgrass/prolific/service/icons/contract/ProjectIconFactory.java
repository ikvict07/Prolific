package org.nevertouchgrass.prolific.service.icons.contract;

import javafx.scene.layout.StackPane;

public interface ProjectIconFactory {
    StackPane configure();

    String getProjectType();
}
