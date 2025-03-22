package org.nevertouchgrass.prolific.service.searching.comparators;

import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.searching.comparators.contract.ProjectComparator;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
public class NameComparator implements ProjectComparator {
    @Override
    public Comparator<Project> addComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(p -> p.getTitle().toLowerCase());
    }

    @Override
    public Comparator<Project> addReversedComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(
                Comparator.comparing((Project p) -> p.getTitle().toLowerCase()).reversed()
        );
    }

    @Override
    public String getComparatorName() {
        return "name";
    }
}
