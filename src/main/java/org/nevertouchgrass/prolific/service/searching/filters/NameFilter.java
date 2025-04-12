package org.nevertouchgrass.prolific.service.searching.filters;

import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.filters.contract.ProjectFilter;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;

@Service
public class NameFilter implements ProjectFilter {
    @Override
    public Predicate<Project> getFilter(Object value) {
        return project -> {
            System.out.println("Got: " + project.getTitle().toLowerCase() + " check against: " + ((String) value).toLowerCase());
            return project.getTitle().toLowerCase().contains(((String) value).toLowerCase());
        };
    }

    @Override
    public Filters getFilterType() {
        return Filters.BY_NAME;
    }
}
