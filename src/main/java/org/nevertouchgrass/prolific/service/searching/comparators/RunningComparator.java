package org.nevertouchgrass.prolific.service.searching.comparators;

import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.process.ProcessService;
import org.nevertouchgrass.prolific.service.searching.comparators.contract.ProjectComparator;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class RunningComparator implements ProjectComparator {
    private final ProcessService processService;

    @Override
    public Comparator<Project> addComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(processService::isProcessRunning);
    }

    @Override
    public Comparator<Project> addReversedComparator(Comparator<Project> comparator) {
        return comparator.thenComparing(Comparator.comparing(processService::isProcessRunning).reversed());
    }

    @Override
    public String getComparatorName() {
        return "running";
    }
}
