package org.nevertouchgrass.prolific.model;

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

/**
 * Stores logs and errors for a process.
 * This class is thread-safe.
 */
@Getter
@ToString
public class ProcessLogs {
    private final Queue<LogWrapper> logs = new PriorityBlockingQueue<>();
    private final List<Consumer<LogWrapper>> onLogAddedListeners = new CopyOnWriteArrayList<>();

    // Maximum number of log entries to keep
    private static final int MAX_LOG_SIZE = 10000;

    public void addOnLogAddedListener(Consumer<LogWrapper> listener) {
        onLogAddedListeners.add(listener);
    }

    public void clearOnLogAddedListeners() {
        onLogAddedListeners.clear();
    }


    public void deleteListener(Consumer<String> listener) {
        onLogAddedListeners.remove(listener);
    }


    /**
     * Adds a log message to the logs list and notifies listeners.
     * If the list exceeds the maximum size, the oldest entries are removed.
     *
     * @param log the log message to add
     */
    public void addLog(LogWrapper log) {
        synchronized (logs) {
            logs.add(log);
            // Remove oldest entries if we exceed the maximum size
            while (logs.size() > MAX_LOG_SIZE) {
                logs.poll();
            }
        }
        onLogAddedListeners.forEach(listener -> listener.accept(log));
    }
}
