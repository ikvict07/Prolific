package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Data
public class ProcessLogs {
    private List<String> logs = new ArrayList<>();
    private List<String> errors = new ArrayList<>();
    private final List<Consumer<String>> onLogAddedListeners = new ArrayList<>();
    private final List<Consumer<String>> onErrorAddedListeners = new ArrayList<>();

    public void addOnLogAddedListener(Consumer<String> listener) {
        onLogAddedListeners.add(listener);
    }

    public void addOnErrorAddedListener(Consumer<String> listener) {
        onErrorAddedListeners.add(listener);
    }

    public void addError(String error) {
        errors.add(error);
        onErrorAddedListeners.forEach(listener -> listener.accept(error));
    }

    public void addLog(String log) {
        logs.add(log);
        onLogAddedListeners.forEach(listener -> listener.accept(log));
    }
}
