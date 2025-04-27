package org.nevertouchgrass.prolific.service.searching.filters;

import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.filters.contract.ProjectFilter;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class IsManuallyAddedFilter implements ProjectFilter {

    @Override
    public Predicate<Project> getFilter(Object value) {
        return project -> project.getIsManuallyAdded().equals(value);
    }

    @Override
    public Filters getFilterType() {
        return Filters.BY_IS_MANUALLY_ADDED;
    }
}
