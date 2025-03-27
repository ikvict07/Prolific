package org.nevertouchgrass.prolific.service.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.LogWrapper;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.service.metrics.ProcessAware;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessLogsService implements ProcessAware {
    private final Map<ProcessWrapper, ProcessLogs> logs = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, Sinks.Many<LogWrapper>> processLogSinks = new ConcurrentHashMap<>();

    public void observeProcess(ProcessWrapper process) {
        logs.computeIfAbsent(process, _ -> new ProcessLogs());
        startLogStreaming(process);
    }

    private void startLogStreaming(ProcessWrapper process) {
        ProcessLogs processLogs = logs.computeIfAbsent(process, _ -> new ProcessLogs());
        Sinks.Many<LogWrapper> sink = Sinks.many().multicast().onBackpressureBuffer();
        processLogSinks.put(process, sink);

        BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getProcess().getInputStream()));
        BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getProcess().getErrorStream()));

        Flux.merge(
                createLogFlux(inputStream, processLogs, LogWrapper.LogType.INFO),
                createLogFlux(errorStream, processLogs, LogWrapper.LogType.ERROR)
        ).subscribe(
                sink::tryEmitNext,
                e -> log.error("Error during log reading", e)
        );
    }

    private Flux<LogWrapper> createLogFlux(BufferedReader reader, ProcessLogs processLogs, LogWrapper.LogType logType) {
        return Flux.<LogWrapper>create(emitter -> {
            try (reader) {
                String line;
                while ((line = reader.readLine()) != null) {
                    var logEntry = new LogWrapper();
                    logEntry.setLog(line);
                    logEntry.setLogType(logType);
                    logEntry.setTimeStamp(LocalDateTime.now());

                    processLogs.addLog(logEntry);
                    emitter.next(logEntry);
                }
                emitter.complete();
            } catch (IOException e) {
                emitter.error(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<LogWrapper> subscribeToLogs(ProcessWrapper process) {
        return processLogSinks.get(process) != null ? processLogSinks.get(process).asFlux() : Flux.empty();
    }

    public Map<ProcessWrapper, ProcessLogs> getLogs() {
        return Map.copyOf(logs);
    }

    @Override
    public void onProcessKill(ProcessWrapper process) {
        Sinks.Many<LogWrapper> sink = processLogSinks.remove(process);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }
}
