package org.nevertouchgrass.prolific.service.searching.comparators;

import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.comparators.contract.ProjectComparator;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class StarredComparator implements ProjectComparator {
    @Override
    public Comparator<Project> addComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(Project::getIsStarred);
    }

    @Override
    public Comparator<Project> addReversedComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(Comparator.comparing(Project::getIsStarred).reversed());
    }

    @Override
    public String getComparatorName() {
        return "starred";
    }
}
