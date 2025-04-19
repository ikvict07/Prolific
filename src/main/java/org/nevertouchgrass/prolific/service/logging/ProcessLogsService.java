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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessLogsService implements ProcessAware {
    private final Map<ProcessWrapper, ProcessLogs> logs = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, Sinks.Many<LogWrapper>> processLogSinks = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, AtomicBoolean> queueProcessingFlags = new ConcurrentHashMap<>();

    private static final long MAX_BATCH_WAIT_MS = 500;

    public void observeProcess(ProcessWrapper process) {
        logs.computeIfAbsent(process, _ -> new ProcessLogs());
        startLogStreaming(process);
    }

    private void startLogStreaming(ProcessWrapper process) {
        ProcessLogs processLogs = logs.computeIfAbsent(process, _ -> new ProcessLogs());
        Sinks.Many<LogWrapper> sink = Sinks.many().multicast().onBackpressureBuffer(1000);
        processLogSinks.put(process, sink);

        BlockingQueue<LogWrapper> logQueue = new LinkedBlockingQueue<>();

        AtomicBoolean isProcessing = new AtomicBoolean(true);
        queueProcessingFlags.put(process, isProcessing);

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getProcess().getInputStream()));
        BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getProcess().getErrorStream()));

        startReader(inputStream, LogWrapper.LogType.INFO, logQueue);
        startReader(errorStream, LogWrapper.LogType.ERROR, logQueue);

        startBatchQueueProcessor(logQueue, sink, processLogs, isProcessing);
    }

    private void startBatchQueueProcessor(
            BlockingQueue<LogWrapper> queue,
            Sinks.Many<LogWrapper> sink,
            ProcessLogs processLogs,
            AtomicBoolean isProcessing
    ) {
        Mono.fromRunnable(() -> {
            try {
                while (isProcessing.get() || !queue.isEmpty()) {
                    List<LogWrapper> batch = new ArrayList<>();

                    LogWrapper firstEntry = isProcessing.get() ? queue.take() : queue.poll();
                    if (firstEntry == null) {
                        continue;
                    }
                    batch.add(firstEntry);
                    drainQueue(queue, batch, isProcessing);
                    batch.forEach(processLogs::addLog);
                    if (batch.size() > 1) {
                        LogWrapper batchedLog = createBatchedLog(batch);
                        emitWithBackoff(sink, batchedLog);
                    } else {
                        emitWithBackoff(sink, firstEntry);
                    }
                }

                sink.tryEmitComplete();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    private void emitWithBackoff(Sinks.Many<LogWrapper> sink, LogWrapper logEntry) {
        if (logEntry.isBatched() && logEntry.getBatchSize() > 1) {
            Stream<LogWrapper> individualLogs = logEntry.getIndividualLogs();
            individualLogs.forEach(sink::tryEmitNext);
        } else {
            sink.tryEmitNext(logEntry);
        }
    }
    private void drainQueue(BlockingQueue<LogWrapper> queue, List<LogWrapper> batch, AtomicBoolean isProcessing)
            throws InterruptedException {
        long startTime = System.currentTimeMillis();

        int targetBatchSize = queue.size() > 1000 ? 200 :
                queue.size() > 500 ? 150 :
                        queue.size() > 100 ? 100 : 50;

        long maxWaitTime = queue.size() > 500 ? 200 : MAX_BATCH_WAIT_MS;

        while (batch.size() < targetBatchSize &&
               System.currentTimeMillis() - startTime < maxWaitTime) {

            LogWrapper entry = queue.poll();
            if (entry == null) {
                if (!isProcessing.get()) {
                    break;
                }
                Thread.sleep(10);
                continue;
            }
            batch.add(entry);
            if (batch.size() >= targetBatchSize) {
                break;
            }
        }
    }

    private void startReader(BufferedReader reader, LogWrapper.LogType logType, BlockingQueue<LogWrapper> queue) {
        Flux.fromStream(reader.lines())
                .map(line -> {
                    var logEntry = new LogWrapper();
                    logEntry.setLog(line);
                    logEntry.setLogType(logType);
                    logEntry.setTimeStamp(System.currentTimeMillis());
                    return logEntry;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(logEntry -> {
                    try {
                        queue.put(logEntry);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                })
                .doFinally(_ -> {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        log.error("Failed to close reader", e);
                    }
                })
                .subscribe();
    }

    private LogWrapper createBatchedLog(List<LogWrapper> batch) {
        LogWrapper first = batch.getFirst();
        LogWrapper batchedLog = new LogWrapper();
        batchedLog.setTimeStamp(first.getTimeStamp());
        batchedLog.setLogType(first.getLogType());

        String combinedText = batch.stream()
                .map(LogWrapper::getLog)
                .collect(Collectors.joining("\n"));

        batchedLog.setLog(combinedText);
        batchedLog.setBatched(true);
        batchedLog.setBatchSize(batch.size());

        return batchedLog;
    }

    @Override
    public void onProcessKill(ProcessWrapper process) {
        AtomicBoolean isProcessing = queueProcessingFlags.get(process);
        if (isProcessing != null) {
            isProcessing.set(false);

            Mono.delay(Duration.ofSeconds(5))
                    .doOnNext(_ -> queueProcessingFlags.remove(process))
                    .subscribe();
        }
    }

    public Flux<LogWrapper> subscribeToLogs(ProcessWrapper process) {
        Sinks.Many<LogWrapper> sink = processLogSinks.get(process);
        return sink != null ? sink.asFlux() : Flux.empty();
    }

    public Map<ProcessWrapper, ProcessLogs> getLogs() {
        return Map.copyOf(logs);
    }
}