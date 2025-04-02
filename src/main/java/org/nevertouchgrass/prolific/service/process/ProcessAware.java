package org.nevertouchgrass.prolific.service.process;

import org.nevertouchgrass.prolific.util.ProcessWrapper;

@FunctionalInterface
public interface ProcessAware {
    void onProcessKill(ProcessWrapper process);
}
