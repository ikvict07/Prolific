package org.nevertouchgrass.prolific.service.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.LogWrapper;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.service.process.ProcessAware;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessLogsService implements ProcessAware {
    private final Map<ProcessWrapper, ProcessLogs> logs = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, Sinks.Many<LogWrapper>> processLogSinks = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, AtomicBoolean> queueProcessingFlags = new ConcurrentHashMap<>();

    public void observeProcess(ProcessWrapper process) {
        logs.computeIfAbsent(process, _ -> new ProcessLogs());
        startLogStreaming(process);
    }

    private void startLogStreaming(ProcessWrapper process) {
        ProcessLogs processLogs = logs.computeIfAbsent(process, _ -> new ProcessLogs());
        Sinks.Many<LogWrapper> sink = Sinks.many().multicast().onBackpressureBuffer();
        processLogSinks.put(process, sink);

        BlockingQueue<LogWrapper> logQueue = new LinkedBlockingQueue<>();

        AtomicBoolean isProcessing = new AtomicBoolean(true);
        queueProcessingFlags.put(process, isProcessing);

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getProcess().getInputStream()));
        BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getProcess().getErrorStream()));

        startReader(inputStream, LogWrapper.LogType.INFO, logQueue);

        startReader(errorStream, LogWrapper.LogType.ERROR, logQueue);

        startQueueProcessor(logQueue, sink, processLogs, isProcessing);
    }

    private void startReader(BufferedReader reader, LogWrapper.LogType logType, BlockingQueue<LogWrapper> queue) {
        Mono.fromRunnable(() -> {
            try (reader) {
                String line;
                while ((line = reader.readLine()) != null) {
                    var logEntry = new LogWrapper();
                    logEntry.setLog(line);
                    logEntry.setLogType(logType);
                    logEntry.setTimeStamp(LocalDateTime.now());

                    queue.put(logEntry);
                }
            } catch (IOException | InterruptedException e) {
                log.error("Error reading process output", e);
                Thread.currentThread().interrupt();
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    private void startQueueProcessor(
            BlockingQueue<LogWrapper> queue,
            Sinks.Many<LogWrapper> sink,
            ProcessLogs processLogs,
            AtomicBoolean isProcessing
    ) {
        Mono.fromRunnable(() -> {
            try {
                while (isProcessing.get() || !queue.isEmpty()) {
                    LogWrapper logEntry = queue.poll();
                    if (logEntry != null) {
                        processLogs.addLog(logEntry);

                        sink.tryEmitNext(logEntry);
                    } else {
                        if (isProcessing.get()) {
                            Thread.sleep(10);
                        }
                    }
                }
                sink.tryEmitComplete();
            } catch (InterruptedException e) {
                log.error("Queue processing interrupted", e);
                Thread.currentThread().interrupt();
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    public Flux<LogWrapper> subscribeToLogs(ProcessWrapper process) {
        return processLogSinks.get(process) != null ? processLogSinks.get(process).asFlux() : Flux.empty();
    }

    public Map<ProcessWrapper, ProcessLogs> getLogs() {
        return Map.copyOf(logs);
    }

    @Override
    public void onProcessKill(ProcessWrapper process) {
        AtomicBoolean isProcessing = queueProcessingFlags.get(process);
        if (isProcessing != null) {
            isProcessing.set(false);
        }

        queueProcessingFlags.remove(process);
    }
}