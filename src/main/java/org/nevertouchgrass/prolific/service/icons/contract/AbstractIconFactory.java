package org.nevertouchgrass.prolific.service.icons.contract;

public abstract class AbstractIconFactory implements ProjectIconFactory {
    private final String projectType;

    protected AbstractIconFactory(String projectType) {
        this.projectType = projectType;
    }

    @Override
    public String getProjectType() {
        return projectType;
    }
}
