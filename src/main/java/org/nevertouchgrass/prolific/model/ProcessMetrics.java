package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Data
public class ProcessMetrics {
    private List<Metric> metrics = Collections.synchronizedList(new ArrayList<>());
    private LocalDateTime startTime;

    public void addMetric(Metric metric) {
        metrics.add(metric);
    }

    public void setStartTime(long startTime) {
        this.startTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTime), ZoneId.systemDefault());
    }
}
