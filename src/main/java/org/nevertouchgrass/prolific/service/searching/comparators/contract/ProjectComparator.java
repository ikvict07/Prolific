package org.nevertouchgrass.prolific.service.searching.comparators.contract;

import org.nevertouchgrass.prolific.model.Project;

import java.util.Comparator;

public interface ProjectComparator {
    Comparator<Project> addComparator(Comparator<Project> comparator);
    Comparator<Project> addReversedComparator(Comparator<Project> comparator);
    String getComparatorName();
}
