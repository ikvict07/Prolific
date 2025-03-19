package org.nevertouchgrass.prolific.service.searching.filters.contract;

import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;

import java.util.function.Function;

public interface ProjectFilter{
    Function<Project, Boolean> getFilter(Object value);
    Filters getFilterType();
}
