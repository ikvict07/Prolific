package org.nevertouchgrass.prolific.service.searching.filters.contract;

import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;

import java.util.function.Predicate;

public interface ProjectFilter{
    Predicate<Project> getFilter(Object value);
    Filters getFilterType();
}
