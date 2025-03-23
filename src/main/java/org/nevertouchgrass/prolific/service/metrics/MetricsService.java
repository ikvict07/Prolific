package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.Metric;
import org.nevertouchgrass.prolific.model.ProcessMetrics;
import org.springframework.stereotype.Service;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class MetricsService implements ProcessAware {
    private final Map<OSProcess, ProcessMetrics> metrics = new ConcurrentHashMap<>();
    private final Set<OSProcess> observableProcesses = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final OperatingSystem os;
    private volatile boolean observing = true;


    public Map<OSProcess, ProcessMetrics> getMetrics() {
        return Map.copyOf(metrics);
    }

    @PostConstruct
    private void init() {
        startObserving();
    }

    public void stopObserving() {
        observing = false;
        executorService.shutdown();
    }

    public void startObserving() {
        executorService.scheduleAtFixedRate(() -> {
            if (!observing) {
                return;
            }
            observableProcesses.forEach(p -> {
                if (os.getProcess(p.getProcessID()) == null) {
                    return;
                }
                var metric = new Metric();
                var cpu = os.getProcess(p.getProcessID()).getProcessCpuLoadCumulative();
                var memory = os.getProcess(p.getProcessID()).getResidentSetSize();
                var threadCount = os.getProcess(p.getProcessID()).getThreadCount();
                var timestamp = LocalDateTime.now();
                metric.setCpuUsage(cpu);
                metric.setMemoryUsage(memory);
                metric.setThreadCount(threadCount);
                metric.setTimeStamp(timestamp);
                metrics.computeIfAbsent(p, p1 -> {
                    var newMetrics = new ProcessMetrics();
                    newMetrics.setStartTime(p1.getStartTime());
                    return newMetrics;
                }).addMetric(metric);
            });
        }, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void onProcessKill(OSProcess process) {
        observableProcesses.remove(process);
    }

    public void observeProcess(OSProcess process) {
        observableProcesses.add(process);
        metrics.computeIfAbsent(process, p -> new ProcessMetrics());
    }

    @PreDestroy
    private void destroy() {
        stopObserving();
    }
}
