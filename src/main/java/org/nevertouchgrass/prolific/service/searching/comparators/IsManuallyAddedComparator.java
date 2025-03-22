package org.nevertouchgrass.prolific.service.searching.comparators;

import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.comparators.contract.ProjectComparator;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class IsManuallyAddedComparator implements ProjectComparator {
    @Override
    public Comparator<Project> addComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(Project::getIsManuallyAdded);
    }
    @Override
    public Comparator<Project> addReversedComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(
                Comparator.comparing(Project::getIsManuallyAdded).reversed()
        );
    }

    @Override
    public String getComparatorName() {
        return "isManuallyAdded";
    }
}
