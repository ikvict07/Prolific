package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Metric;
import org.nevertouchgrass.prolific.model.ProcessMetrics;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.nevertouchgrass.prolific.util.ProcessUtl.receiveDescendants;

@Service
@RequiredArgsConstructor
@Log4j2
public class MetricsService implements ProcessAware {
    private final Map<ProcessWrapper, ProcessMetrics> metrics = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, Sinks.Many<Metric>> metricSinks = new ConcurrentHashMap<>();

    private final AtomicBoolean observing = new AtomicBoolean(true);

    private final Map<ProcessWrapper, AtomicBoolean> processActiveFlags = new ConcurrentHashMap<>();
    private final NotificationService notificationService;
    private final LocalizationProvider localizationProvider;

    public void stopObserving() {
        observing.set(false);
        processActiveFlags.forEach((_, flag) -> flag.set(false));
    }

    public void observeProcess(ProcessWrapper process) {
        metrics.computeIfAbsent(process, p -> {
            var newMetrics = new ProcessMetrics();
            newMetrics.setStartTime(p.getProcess().toHandle().info().startInstant().orElse(Instant.now()).toEpochMilli());
            return newMetrics;
        });

        Sinks.Many<Metric> sink = Sinks.many().multicast().onBackpressureBuffer();
        metricSinks.put(process, sink);

        AtomicBoolean processActive = new AtomicBoolean(true);
        processActiveFlags.put(process, processActive);

        startMetricsCollection(process, sink, processActive);
    }

    private void startMetricsCollection(ProcessWrapper process, Sinks.Many<Metric> sink, AtomicBoolean processActive) {
        Flux.interval(Duration.ofSeconds(1))
                .takeWhile(_ -> observing.get() && processActive.get())
                .flatMap(_ -> collectMetrics(process))
                .doOnNext(metric -> {
                    var processMetrics = metrics.get(process);
                    if (processMetrics != null) {
                        processMetrics.addMetric(metric);
                    }

                    sink.tryEmitNext(metric);
                })
                .doOnComplete(() -> {
                    sink.tryEmitComplete();
                    log.debug("Completed metrics collection for process: {}", process.getProcess().pid());
                })
                .doOnError(e -> log.error("Error collecting metrics for process: {}", process.getProcess().pid(), e))
                .subscribe();
    }


    private Mono<Metric> collectMetrics(ProcessWrapper process) {
        return Mono.zip(
                        Mono.fromCallable(() -> getCpuUsage(process)).subscribeOn(Schedulers.boundedElastic()),
                        Mono.fromCallable(() -> getMemoryUsage(process)).subscribeOn(Schedulers.boundedElastic())
                )
                .map(tuple -> {
                    var metric = new Metric();
                    metric.setCpuUsage(tuple.getT1());
                    metric.setMemoryUsage(tuple.getT2());
                    metric.setTimeStamp(LocalDateTime.now());
                    return metric;
                })
                .filter(Objects::nonNull);
    }

    private double getCpuUsage(ProcessWrapper process) {
        var descendants = new HashSet<ProcessHandle>();
        receiveDescendants(process.getProcess().toHandle(), descendants);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return getCpuForWindows(descendants);
        } else {
            return getCpuForUnix(descendants);
        }
    }

    private long getMemoryUsage(ProcessWrapper process) {
        var descendants = new HashSet<ProcessHandle>();
        receiveDescendants(process.getProcess().toHandle(), descendants);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return getMemoryUsageForWindows(descendants);
        } else {
            return getMemoryUsageForUnix(descendants);
        }
    }

    private long getMemoryUsageForWindows(Set<ProcessHandle> descendants) {
        AtomicReference<Long> memoryUsage = new AtomicReference<>(0L);
        descendants.forEach(d -> {
            var pb = new ProcessBuilder("wmic", "path", "Win32_PerfFormattedData_PerfProc_Process", "where", "IDProcess=" + d.pid(), "get", "WorkingSetPrivate");
            readMemoryUsage(memoryUsage, pb);
        });
        return memoryUsage.get();
    }

    private long getMemoryUsageForUnix(Set<ProcessHandle> descendants) {
        AtomicReference<Long> memoryUsage = new AtomicReference<>(0L);
        descendants.forEach(d -> {
            var pb = new ProcessBuilder("ps", "-p", String.valueOf(d.pid()), "-o", "rss");
            readMemoryUsage(memoryUsage, pb);
        });
        return memoryUsage.get();
    }

    private double getCpuForUnix(Set<ProcessHandle> descendants) {
        AtomicReference<Double> cpuUsage = new AtomicReference<>(0d);
        descendants.forEach(d -> {
            var pb = new ProcessBuilder("ps", "-p", String.valueOf(d.pid()), "-o", "%cpu");
            cpuUsage.updateAndGet(v -> v + readCpuUsage(pb));
        });
        return cpuUsage.get();
    }

    private double getCpuForWindows(Set<ProcessHandle> descendants) {
        AtomicReference<Double> cpuUsage = new AtomicReference<>(0d);
        descendants.forEach(d -> {
            var pb = new ProcessBuilder("wmic", "path", "Win32_PerfFormattedData_PerfProc_Process", "where", "IDProcess=" + d.pid(), "get", "PercentProcessorTime");
            cpuUsage.updateAndGet(v -> v + readCpuUsage(pb));
        });
        return cpuUsage.get();
    }

    private void readMemoryUsage(AtomicReference<Long> memoryUsage, ProcessBuilder pb) {
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_reading_ram()));
        }
        var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(p).getInputStream()));
        String line;
        int i = 0;
        while (i < 4) {
            try {
                if ((reader.ready() && (line = reader.readLine()) != null)) {
                    try {
                        String finalLine = line;
                        memoryUsage.updateAndGet(v -> (v + Long.parseLong(finalLine.trim().replaceAll("[^0-9.]", "")) * 1024L));
                        break;
                    } catch (Exception e) {
                        i++;
                    }
                }
            } catch (IOException e) {
                notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_reading_ram()));
                break;
            }
        }
    }

    private double readCpuUsage(ProcessBuilder pb) {
        AtomicReference<Double> cpuUsage = new AtomicReference<>(0d);
        Process p = null;
        try {
            p = pb.start();
        } catch (IOException e) {
            notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_reading_cpu()));
        }
        var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(p).getInputStream()));
        String line;
        int i = 0;
        while (i < 4) {
            try {
                if ((reader.ready() && (line = reader.readLine()) != null)) {
                    try {
                        String finalLine = line;
                        cpuUsage.updateAndGet(v -> (v + Double.parseDouble(finalLine.trim().replaceAll("[^0-9.]", ""))));
                        break;
                    } catch (Exception e) {
                        i++;
                    }
                }
            } catch (IOException e) {
                notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_reading_cpu()));
                break;
            }
        }
        return cpuUsage.get();
    }

    public Flux<Metric> subscribeToMetrics(ProcessWrapper process) {
        return metricSinks.get(process) != null ? metricSinks.get(process).asFlux() : Flux.empty();
    }

    @Override
    public void onProcessKill(ProcessWrapper process) {
        AtomicBoolean processActive = processActiveFlags.remove(process);
        if (processActive != null) {
            processActive.set(false);
        }

        Sinks.Many<Metric> sink = metricSinks.remove(process);
        if (sink != null) {
            sink.tryEmitComplete();
        }
    }

    @PreDestroy
    private void destroy() {
        stopObserving();
    }
}