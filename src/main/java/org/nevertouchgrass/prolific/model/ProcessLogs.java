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
    private static final int MAX_LOG_SIZE = 10000;

    public void addLog(LogWrapper log) {
        synchronized (logs) {
            logs.add(log);
            while (logs.size() > MAX_LOG_SIZE) {
                logs.poll();
            }
        }
    }
}
