package org.nevertouchgrass.prolific.constants.action;

import org.nevertouchgrass.prolific.model.Project;

public record DeleteProjectAction(
        Project project
) implements IAction {
}
