package org.nevertouchgrass.prolific.service.logging;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.service.metrics.ProcessService;
import org.springframework.stereotype.Service;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessLogsService {
    private final List<Process> observableProcesses = new CopyOnWriteArrayList<>();
    private final Map<Process, ProcessLogs> logs = new ConcurrentHashMap<>();
    private final ProcessService processService;
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final OperatingSystem os;
    private final Map<Process, BufferedReader> processInputStreamMap = new ConcurrentHashMap<>();
    private final Map<Process, BufferedReader> processErrorStreamMap = new ConcurrentHashMap<>();
    private volatile boolean observing = true;

    @PostConstruct
    private void init() {
        processService.registerOnKillListener(this::onProcessKill);
        startObserving();
    }

    public Map<Process, ProcessLogs> getLogs() {
        return Map.copyOf(logs);
    }

    public void observeProcess(Process process) {
        observableProcesses.add(process);
        logs.computeIfAbsent(process, p -> new ProcessLogs());
    }

    public void onProcessKill(OSProcess process) {
        observableProcesses.removeIf(p -> (int) p.pid() == process.getProcessID());
    }

    public void startObserving() {
        executorService.scheduleAtFixedRate(() -> {
            if (!observing) {
                return;
            }
            observableProcesses.forEach(p -> {
                if (os.getProcess((int) p.pid()) == null) {
                    return;
                }
                var processLogs = logs.computeIfAbsent(p, p1 -> new ProcessLogs());
                var inputStream = processInputStreamMap.computeIfAbsent(p, p1 -> new BufferedReader(new InputStreamReader(p1.getInputStream())));
                var errorStream = processErrorStreamMap.computeIfAbsent(p, p1 -> new BufferedReader(new InputStreamReader(p1.getErrorStream())));
                readNewLines(inputStream, processLogs);
                readNewError(errorStream, processLogs);
            });
        }, 0, 3, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stopObserving() {
        observing = false;
        executorService.shutdown();
    }

    private void readNewLines(BufferedReader reader, ProcessLogs processLogs) {
        try {
            String line;
            while (reader.ready() && (line = reader.readLine()) != null){
                processLogs.addLog(line);
            }
        } catch (IOException e) {
            log.error("Error occurred while reading input stream", e);
        }
    }

    private void readNewError(BufferedReader reader, ProcessLogs processLogs) {
        try {
            String line;
            while (reader.ready() && (line = reader.readLine()) != null) {
                processLogs.addError(line);
            }
        } catch (IOException e) {
            log.error("Error occurred while reading error stream", e);
        }
    }

}
