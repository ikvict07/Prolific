package org.nevertouchgrass.prolific.service.searching.comparators;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.comparators.contract.ProjectComparator;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectComparatorBuilder {
    private final List<ProjectComparator> comparators;


    @Data
    @AllArgsConstructor
    public static class ComparatorOption {
        String optionName;
        /**
         * 0 - do not use
         * 1 - default ordering by parameter
         * -1 - reverse ordering by parameter
         */
        int value;
    }

    public Comparator<Project> build(ComparatorOption... options) {
        var optionsList = List.of(options);
        var vals = optionsList.stream().map(ComparatorOption::getValue).collect(Collectors.toSet());
        if (vals.contains(0) && vals.size() == 1) {
            return getDefault();
        }
        Comparator<Project> result = emptyComparator();

        for (ComparatorOption option : options) {
            final Comparator<Project> currentComparator = result;
            for (ProjectComparator comparator : comparators) {
                if (comparator.getComparatorName().equalsIgnoreCase(option.optionName)) {
                    if (option.value == 1) {
                        result = comparator.addComparator(currentComparator);
                    } else if (option.value == -1) {
                        result = comparator.addReversedComparator(currentComparator);
                    }
                    break;
                }
            }
        }
        return result;
    }

    public static Comparator<Project> getDefault() {
        return Comparator.comparing(Project::getIsStarred).reversed().thenComparing(p -> p.getTitle().toLowerCase());
    }

    public Comparator<Project> emptyComparator() {
        return (_, _) -> 0;
    }
}
