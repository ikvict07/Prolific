package org.nevertouchgrass.prolific.service.logging;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.service.metrics.ProcessAware;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
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
    private final OperatingSystem os;
    private final List<ProcessWrapper> observableProcesses = new CopyOnWriteArrayList<>();
    private final Map<ProcessWrapper, ProcessLogs> logs = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
        new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setName("process-logs-service-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        }
    );
    private final Map<ProcessWrapper, BufferedReader> processInputStreamMap = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, BufferedReader> processErrorStreamMap = new ConcurrentHashMap<>();
    private volatile boolean observing = true;

    /**
     * Initializes the service and starts observing processes.
     */
    @PostConstruct
    public void init() {
        startObserving();
    }

    public Map<ProcessWrapper, ProcessLogs> getLogs() {
        return Map.copyOf(logs);
    }

    public void observeProcess(ProcessWrapper process) {
        observableProcesses.add(process);
        logs.computeIfAbsent(process, _ -> new ProcessLogs());
    }

    /**
     * Handles process termination by cleaning up resources.
     *
     * @param process the terminated process
     */
    @Override
    public void onProcessKill(ProcessWrapper process) {
        observableProcesses.removeIf(p -> {
            if (p.equals(process)) {
                // Clean up resources for this process
                readLogs(p);
                closeReaders(p);
                return true;
            }
            return false;
        });
    }

    private void readLogs(ProcessWrapper p) {
        var processLogs = logs.computeIfAbsent(p, _ -> new ProcessLogs());
        var logsReader = processInputStreamMap.computeIfAbsent(p, _ -> new BufferedReader(new InputStreamReader(p.getProcess().getInputStream())));
        var errorsReader = processErrorStreamMap.computeIfAbsent(p, _ -> new BufferedReader(new InputStreamReader(p.getProcess().getErrorStream())));
        readNewLines(logsReader, processLogs);
        readNewError(errorsReader, processLogs);
    }

    /**
     * Closes readers associated with a process and removes them from maps.
     *
     * @param process the process whose readers should be closed
     */
    @SuppressWarnings("EmptyTryBlock")
    private void closeReaders(ProcessWrapper process) {

        try (BufferedReader inputReader = processInputStreamMap.remove(process)) {
            // closing
        } catch (IOException e) {
            log.error("Error closing input stream reader for process {}", process.getProcess().pid(), e);
        }

        try (BufferedReader errorReader = processErrorStreamMap.remove(process)) {
            // closing
        } catch (IOException e) {
            log.error("Error closing error stream reader for process {}", process.getProcess().pid(), e);
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
            List<ProcessWrapper> processesToRemove = new CopyOnWriteArrayList<>();

            observableProcesses.forEach(p -> {
                if (os.getProcess(p.getOsProcess().getProcessID()) == null) {
                    // Process no longer exists, add it to the list for removal
                    processesToRemove.add(p);
                    return;
                }
                readLogs(p);
            });

            // Clean up resources for processes that no longer exist
            processesToRemove.forEach(p -> {
                closeReaders(p);
                logs.remove(p);
                observableProcesses.remove(p);
            });
        }, 0, 1, TimeUnit.SECONDS);
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
