package org.nevertouchgrass.prolific.model;

import lombok.Data;

@Data
public class LogWrapper implements Comparable<LogWrapper>{
    @Override
    public int compareTo(LogWrapper o) {
        return Long.compare(timeStamp, o.timeStamp);
    }

    private String log;
    private LogType logType;
    private long timeStamp;

    public enum LogType {
        INFO,
        ERROR
    }
}
