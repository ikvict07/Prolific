package org.nevertouchgrass.prolific.model;

import java.time.LocalDateTime;

public record TerminatedProcessInfo(
        Project project,
        RunConfig runConfig,
        ProcessLogs logs,
        ProcessMetrics metrics
) {}

