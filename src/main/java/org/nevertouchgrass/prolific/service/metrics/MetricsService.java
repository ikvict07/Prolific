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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private final Map<Integer, OSProcess> priorSnapshotMap = new ConcurrentHashMap<>();

    private Mono<Metric> collectMetrics(ProcessWrapper process) {
        return Mono.fromCallable(() -> {
                    int pid = process.getOsProcess().getProcessID();
                    OSProcess osProcess = os.getProcess(pid);

                    if (osProcess == null) {
                        return null;
                    }

                    var metric = new Metric();

                    var cpuUsage = osProcess.getProcessCpuLoadCumulative() * 100;
                    if (priorSnapshotMap.containsKey(pid)) {
                        cpuUsage = osProcess.getProcessCpuLoadBetweenTicks(priorSnapshotMap.get(pid)) * 100;
                        System.out.println("Current pros is: " + osProcess + " and prior is: " + priorSnapshotMap.get(pid));
                        System.out.println("Cpu of current is: " + cpuUsage + " and prior is: " + priorSnapshotMap.get(pid).getProcessCpuLoadCumulative());
                        System.out.println("Uptime of current is: " + osProcess.getUpTime() + " and prior is: " + priorSnapshotMap.get(pid).getUpTime());
                        System.out.println("Kernel time of current is: " + osProcess.getKernelTime() + " and prior is: " + priorSnapshotMap.get(pid).getKernelTime());
                        System.out.println("User time of current is: " + osProcess.getUserTime() + " and prior is: " + priorSnapshotMap.get(pid).getUserTime());
                        osProcess.getProcessCpuLoadCumulative();
                    }

                    System.out.println(cpuUsage);
                    metric.setCpuUsage(cpuUsage);
                    metric.setMemoryUsage(osProcess.getResidentSetSize());
                    metric.setThreadCount(osProcess.getThreadCount());
                    metric.setTimeStamp(LocalDateTime.now());
                    priorSnapshotMap.put(pid, osProcess);
                    return metric;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .filter(Objects::nonNull);
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