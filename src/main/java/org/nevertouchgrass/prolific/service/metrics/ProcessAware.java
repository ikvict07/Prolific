package org.nevertouchgrass.prolific.service.metrics;

import org.nevertouchgrass.prolific.util.ProcessWrapper;

@FunctionalInterface
public interface ProcessAware {
    void onProcessKill(ProcessWrapper process);
}
