package org.nevertouchgrass.prolific.service.logging;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.service.metrics.ProcessAware;
import org.springframework.stereotype.Service;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessLogsService implements ProcessAware {
    private final List<Process> observableProcesses = new CopyOnWriteArrayList<>();
    private final Map<Process, ProcessLogs> logs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setName("process-logs-service-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        }
    );
    private final OperatingSystem os;
    private final Map<Process, BufferedReader> processInputStreamMap = new ConcurrentHashMap<>();
    private final Map<Process, BufferedReader> processErrorStreamMap = new ConcurrentHashMap<>();
    private volatile boolean observing = true;

    /**
     * Initializes the service and starts observing processes.
     */
    @PostConstruct
    public void init() {
        startObserving();
    }

    public Map<Process, ProcessLogs> getLogs() {
        return Map.copyOf(logs);
    }

    public void observeProcess(Process process) {
        observableProcesses.add(process);
        logs.computeIfAbsent(process, p -> new ProcessLogs());
    }

    /**
     * Handles process termination by cleaning up resources.
     *
     * @param process the terminated process
     */
    @Override
    public void onProcessKill(OSProcess process) {
        observableProcesses.removeIf(p -> {
            if ((int) p.pid() == process.getProcessID()) {
                // Clean up resources for this process
                closeReaders(p);
                return true;
            }
            return false;
        });
    }

    /**
     * Closes readers associated with a process and removes them from maps.
     *
     * @param process the process whose readers should be closed
     */
    private void closeReaders(Process process) {

        try (BufferedReader inputReader = processInputStreamMap.remove(process)) {
            // closing
        } catch (IOException e) {
            log.error("Error closing input stream reader for process {}", process.pid(), e);
        }

        try (BufferedReader errorReader = processErrorStreamMap.remove(process)) {
            // closing
        } catch (IOException e) {
            log.error("Error closing error stream reader for process {}", process.pid(), e);
        }
    }

    /**
     * Starts observing processes for new log entries.
     * This method schedules a task that periodically checks for new log entries
     * and cleans up resources for processes that no longer exist.
     */
    public void startObserving() {
        executorService.scheduleAtFixedRate(() -> {
            if (!observing) {
                return;
            }

            // Create a list to store processes that no longer exist
            List<Process> processesToRemove = new CopyOnWriteArrayList<>();

            observableProcesses.forEach(p -> {
                if (os.getProcess((int) p.pid()) == null) {
                    // Process no longer exists, add it to the list for removal
                    processesToRemove.add(p);
                    return;
                }
                var processLogs = logs.computeIfAbsent(p, p1 -> new ProcessLogs());
                var inputStream = processInputStreamMap.computeIfAbsent(p, p1 -> new BufferedReader(new InputStreamReader(p1.getInputStream())));
                var errorStream = processErrorStreamMap.computeIfAbsent(p, p1 -> new BufferedReader(new InputStreamReader(p1.getErrorStream())));
                readNewLines(inputStream, processLogs);
                readNewError(errorStream, processLogs);
            });

            // Clean up resources for processes that no longer exist
            processesToRemove.forEach(p -> {
                closeReaders(p);
                logs.remove(p);
                observableProcesses.remove(p);
            });
        }, 0, 3, TimeUnit.SECONDS);
    }

    /**
     * Stops observing processes and cleans up resources.
     * This method is called when the application is shutting down.
     */
    @PreDestroy
    public void stopObserving() {
        observing = false;

        // Clean up all resources
        observableProcesses.forEach(this::closeReaders);
        processInputStreamMap.clear();
        processErrorStreamMap.clear();

        // Shutdown the executor service
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Error shutting down executor service", e);
        }
    }

    /**
     * Reads new lines from the input stream and adds them to the process logs.
     *
     * @param reader the reader for the input stream
     * @param processLogs the logs to add the lines to
     */
    private void readNewLines(BufferedReader reader, ProcessLogs processLogs) {
        try {
            String line;
            while (reader.ready() && (line = reader.readLine()) != null) {
                processLogs.addLog(line);
            }
        } catch (IOException e) {
            log.error("Error occurred while reading input stream", e);
        }
    }

    /**
     * Reads new lines from the error stream and adds them to the process logs.
     *
     * @param reader the reader for the error stream
     * @param processLogs the logs to add the lines to
     */
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
