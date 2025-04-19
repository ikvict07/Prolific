package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.util.Arrays;
import java.util.stream.Stream;

@Data
public class LogWrapper implements Comparable<LogWrapper>{
    @Override
    public int compareTo(LogWrapper o) {
        return Long.compare(timeStamp, o.timeStamp);
    }

    private String log;
    private LogType logType;
    private long timeStamp;
    private boolean batched = false;
    private int batchSize = 1;

    public Stream<LogWrapper> getIndividualLogs() {
        return Arrays.stream(log.split("\n")).map(l -> {
            LogWrapper logWrapper = new LogWrapper();
            logWrapper.setLog(l);
            logWrapper.setLogType(logType);
            logWrapper.setTimeStamp(timeStamp);
            return logWrapper;
        });
    }

    public enum LogType {
        INFO,
        ERROR
    }
}
