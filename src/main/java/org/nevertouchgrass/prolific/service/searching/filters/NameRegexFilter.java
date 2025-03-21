package org.nevertouchgrass.prolific.service.searching.filters;

import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.filters.contract.ProjectFilter;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class NameRegexFilter implements ProjectFilter {
    @Override
    public Function<Project, Boolean> getFilter(Object value) {
        return project -> project.getTitle().matches((String) value);
    }

    @Override
    public Filters getFilterType() {
        return Filters.BY_NAME_REGEX;
    }
}
