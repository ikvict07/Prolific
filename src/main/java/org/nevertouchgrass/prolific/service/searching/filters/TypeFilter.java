package org.nevertouchgrass.prolific.service.searching.filters;

import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.filters.contract.ProjectFilter;

import java.util.function.Function;

public class TypeFilter implements ProjectFilter {
    @Override
    public Function<Project, Boolean> getFilter(Object value) {
        return project -> project.getType().equalsIgnoreCase((String) value);
    }

    @Override
    public Filters getFilterType() {
        return Filters.BY_TYPE;
    }
}
