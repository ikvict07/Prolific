package org.nevertouchgrass.prolific.service.metrics;

import oshi.software.os.OSProcess;

@FunctionalInterface
public interface ProcessAware {
    void onProcessKill(OSProcess process);
}
