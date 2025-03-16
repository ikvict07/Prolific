package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Data
public class ProcessMetrics {
    private List<Metric> metrics = new CopyOnWriteArrayList<>();
    private LocalDateTime startTime;
    private final List<Consumer<Metric>> onAddListeners = new CopyOnWriteArrayList<>();


    public void addMetric(Metric metric) {
        metrics.add(metric);
        onAddListeners.forEach(listener -> listener.accept(metric));
    }

    public void registerOnAddListener(Consumer<Metric> consumer) {
        onAddListeners.add(consumer);
    }

    public void setStartTime(long startTime) {
        this.startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
    }
}
