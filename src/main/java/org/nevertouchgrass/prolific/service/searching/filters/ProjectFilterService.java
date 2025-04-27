package org.nevertouchgrass.prolific.service.searching.filters;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.filters.contract.ProjectFilter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class ProjectFilterService {
    private final List<ProjectFilter> filters;

    @Data
    @AllArgsConstructor
    public static class FilterOption {
        private Filters filterType;
        private Object value;
    }

    public Predicate<Project> getFilter(FilterOption... filterOptions) {
        var options = List.of(filterOptions);
        var result = new ArrayList<Predicate<Project>>();
        for (var option : options) {
            var matchingFilter = filters.stream().filter(f -> f.getFilterType().equals(option.filterType)).findFirst();
            if (matchingFilter.isEmpty()) {
                continue;
            }
            result.add(matchingFilter.get().getFilter(option.value));
        }
        return (project -> result.stream().allMatch(f -> f.test(project)));
    }

    public static Predicate<Project> getDefaultFilter() {
        return _ -> true;
    }
}
