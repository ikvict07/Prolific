package org.nevertouchgrass.prolific.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Stores logs and errors for a process.
 * This class is thread-safe.
 */
@Getter
@ToString
public class ProcessLogs {
    private final Queue<LogWrapper> logs = new PriorityBlockingQueue<>();

    public void addLog(LogWrapper log) {
        logs.add(log);
    }
}
