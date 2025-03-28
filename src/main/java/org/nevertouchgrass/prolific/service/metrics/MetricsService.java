package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Metric;
import org.nevertouchgrass.prolific.model.ProcessMetrics;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Log4j2
public class MetricsService implements ProcessAware {
    private final Map<ProcessWrapper, ProcessMetrics> metrics = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, Sinks.Many<Metric>> metricSinks = new ConcurrentHashMap<>();

    private final OperatingSystem os;
    private final AtomicBoolean observing = new AtomicBoolean(true);

    private final Map<ProcessWrapper, AtomicBoolean> processActiveFlags = new ConcurrentHashMap<>();

    public Map<ProcessWrapper, ProcessMetrics> getMetrics() {
        return Map.copyOf(metrics);
    }

    public void stopObserving() {
        observing.set(false);
        processActiveFlags.forEach((process, flag) -> flag.set(false));
    }

    public void observeProcess(ProcessWrapper process) {
        metrics.computeIfAbsent(process, p -> {
            var newMetrics = new ProcessMetrics();
            newMetrics.setStartTime(p.getOsProcess().getStartTime());
            return newMetrics;
        });

        Sinks.Many<Metric> sink = Sinks.many().multicast().onBackpressureBuffer();
        metricSinks.put(process, sink);

        AtomicBoolean processActive = new AtomicBoolean(true);
        processActiveFlags.put(process, processActive);

        startMetricsCollection(process, sink, processActive);
    }

    private void startMetricsCollection(ProcessWrapper process, Sinks.Many<Metric> sink, AtomicBoolean processActive) {
        Flux.interval(Duration.ofSeconds(3))
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
        return Mono.fromCallable(() -> {
                    int pid = process.getPid();
                    OSProcess osProcess = os.getProcess(pid);
                    if (osProcess == null) {
                        return null;
                    }
                    var metric = new Metric();
                    metric.setCpuUsage(getCpuUsage(process));
                    metric.setMemoryUsage(getMemoryUsage(process));
                    metric.setThreadCount(osProcess.getThreadCount());
                    metric.setTimeStamp(LocalDateTime.now());
                    return metric;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Objects::nonNull);
    }

    private double getCpuUsage(ProcessWrapper process) {
        var descendants = new HashSet<ProcessHandle>();
        receiveDescendants(process.getProcess().toHandle(), descendants);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return getCpuForWindows(process, descendants);
        } else {
            return getCpuForUnix(process, descendants);
        }
    }

    private long getMemoryUsage(ProcessWrapper process) {
        var descendants = new HashSet<ProcessHandle>();
        receiveDescendants(process.getProcess().toHandle(), descendants);
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            return getMemoryUsageForWindows(process, descendants);
        } else {
            return getMemoryUsageForUnix(process, descendants);
        }
    }

    private long getMemoryUsageForWindows(ProcessWrapper process, Set<ProcessHandle> descendants) {
        AtomicReference<Long> memoryUsage = new AtomicReference<>(0L);
        descendants.forEach(d -> {
            var pb = new ProcessBuilder("wmic", "path", "Win32_PerfFormattedData_PerfProc_Process", "where", "IDProcess=" + d.pid(), "get", "WorkingSetSize");
            Process p = null;
            try {
                p = pb.start();
            } catch (IOException e) {

            }
            var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                try {
                    if ((reader.ready() && (line = reader.readLine()) != null)) {
                        try {
                            String finalLine = line;
                            memoryUsage.updateAndGet(v -> (v + Long.parseLong(finalLine.trim().replaceAll("[^0-9]", "")) * 1024L));
                            break;
                        } catch (Exception e) {
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        });
        return memoryUsage.get();
    }

    private long getMemoryUsageForUnix(ProcessWrapper process, Set<ProcessHandle> descendants) {
        AtomicReference<Long> memoryUsage = new AtomicReference<>(0L);
        descendants.forEach(d -> {
            var pb = new ProcessBuilder("ps", "-p", String.valueOf(d.pid()), "-o", "rss");
            Process p = null;
            try {
                p = pb.start();
            } catch (IOException e) {

            }
            var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                try {
                    if ((reader.ready() && (line = reader.readLine()) != null)) {
                        try {
                            String finalLine = line;
                            memoryUsage.updateAndGet(v -> (v + Long.parseLong(finalLine.trim().replaceAll("[^0-9]", "")) * 1024L));
                            break;
                        } catch (Exception e) {
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        });
        return memoryUsage.get();
    }

    private double getCpuForUnix(ProcessWrapper process, Set<ProcessHandle> descendants) {
        AtomicReference<Double> cpuUsage = new AtomicReference<>(0d);
        descendants.forEach(d -> {

            var pb = new ProcessBuilder("ps", "-p", String.valueOf(d.pid()), "-o", "%cpu");
            Process p = null;
            try {
                p = pb.start();
            } catch (IOException e) {

            }
            var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                try {
                    if ((reader.ready() && (line = reader.readLine()) != null)) {
                        try {
                            String finalLine = line;
                            cpuUsage.updateAndGet(v -> (v + Double.parseDouble(finalLine.trim().replaceAll("[^0-9]", ""))));
                            break;
                        } catch (Exception e) {
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        });
        return cpuUsage.get();
    }

    private double getCpuForWindows(ProcessWrapper process, Set<ProcessHandle> descendants) {
        AtomicReference<Double> cpuUsage = new AtomicReference<>(0d);
        descendants.forEach(d -> {

            var pb = new ProcessBuilder("wmic", "path", "Win32_PerfFormattedData_PerfProc_Process", "where", "IDProcess=" + d.pid(), "get", "PercentProcessorTime");
            Process p = null;
            try {
                p = pb.start();
            } catch (IOException e) {

            }
            var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while (true) {
                try {
                    if ((reader.ready() && (line = reader.readLine()) != null)) {
                        try {
                            String finalLine = line;
                            cpuUsage.updateAndGet(v -> (v + Double.parseDouble(finalLine.trim().replaceAll("[^0-9]", ""))));
                            break;
                        } catch (Exception e) {
                        }
                    }
                } catch (IOException e) {
                    break;
                }
            }
        });
        return cpuUsage.get();
    }

    public void receiveDescendants(ProcessHandle process, Set<ProcessHandle> descendantsToReceive) {
        var d = new HashSet<ProcessHandle>();
        var children = process.children().toList();
        var descendants = process.descendants().toList();
        d.addAll(children);
        d.addAll(descendants);
        descendantsToReceive.addAll(d);
        children.forEach(c -> receiveDescendants(c, descendantsToReceive));
        children.forEach(c -> receiveDescendants(c, descendantsToReceive));
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