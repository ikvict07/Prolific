package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Metric {
    private double cpuUsage;
    private double memoryUsage;
    private double threadCount;
    private LocalDateTime timeStamp;
}
