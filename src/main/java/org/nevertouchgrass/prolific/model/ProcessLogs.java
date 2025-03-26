package org.nevertouchgrass.prolific.model;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Stores logs and errors for a process.
 * This class is thread-safe.
 */
@Getter
@ToString
public class ProcessLogs {
    private final List<String> logs = new CopyOnWriteArrayList<>();
    private final List<String> errors = Collections.synchronizedList(new ArrayList<>());
    private final List<Consumer<String>> onLogAddedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<String>> onErrorAddedListeners = new CopyOnWriteArrayList<>();

    // Maximum number of log entries to keep
    private static final int MAX_LOG_SIZE = 10000;
    private static final int MAX_ERROR_SIZE = 5000;

    public void addOnLogAddedListener(Consumer<String> listener) {
        onLogAddedListeners.add(listener);
    }

    public void clearOnLogAddedListeners() {
        onLogAddedListeners.clear();
    }

    public void addOnErrorAddedListener(Consumer<String> listener) {
        onErrorAddedListeners.add(listener);
    }

    /**
     * Adds an error message to the errors list and notifies listeners.
     * If the list exceeds the maximum size, the oldest entries are removed.
     *
     * @param error the error message to add
     */
    public void addError(String error) {
        synchronized (errors) {
            errors.add(error);
            // Remove oldest entries if we exceed the maximum size
            if (errors.size() > MAX_ERROR_SIZE) {
                errors.subList(0, errors.size() - MAX_ERROR_SIZE).clear();
            }
        }
        onErrorAddedListeners.forEach(listener -> listener.accept(error));
    }

    /**
     * Adds a log message to the logs list and notifies listeners.
     * If the list exceeds the maximum size, the oldest entries are removed.
     *
     * @param log the log message to add
     */
    public void addLog(String log) {
        synchronized (logs) {
            logs.add(log);
            // Remove oldest entries if we exceed the maximum size
            if (logs.size() > MAX_LOG_SIZE) {
                logs.subList(0, logs.size() - MAX_LOG_SIZE).clear();
            }
        }
        onLogAddedListeners.forEach(listener -> listener.accept(log));
    }
}
