package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LogWrapper implements Comparable<LogWrapper>{
    @Override
    public int compareTo(LogWrapper o) {
        return timeStamp.compareTo(o.timeStamp);
    }

    private String log;
    private LogType logType;
    private LocalDateTime timeStamp;

    public enum LogType {
        INFO,
        ERROR
    }
}
