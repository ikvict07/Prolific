package org.nevertouchgrass.prolific.constants.action;

import org.nevertouchgrass.prolific.model.Project;

public record ExcludeProjectAction(
        Project project
) implements IAction {}
