package org.nevertouchgrass.prolific.model;

public record TerminatedProcessInfo(
        Project project,
        RunConfig runConfig,
        ProcessLogs logs,
        ProcessMetrics metrics
) {}

