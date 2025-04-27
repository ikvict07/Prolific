package org.nevertouchgrass.prolific.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TerminatedProcessInfo {
    private final Project project;
    private final RunConfig runConfig;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;
    private final int exitCode;
    private final ProcessLogs logs;
    private final ProcessMetrics metrics;
}
