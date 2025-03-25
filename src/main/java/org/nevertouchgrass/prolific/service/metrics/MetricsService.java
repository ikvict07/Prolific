package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.Metric;
import org.nevertouchgrass.prolific.model.ProcessMetrics;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
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
    private final Map<ProcessWrapper, ProcessMetrics> metrics = new ConcurrentHashMap<>();
    private final Set<ProcessWrapper> observableProcesses = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final OperatingSystem os;
    private volatile boolean observing = true;


    public Map<ProcessWrapper, ProcessMetrics> getMetrics() {
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
                OSProcess process = p.getOsProcess();
                if (os.getProcess(process.getProcessID()) == null) {
                    return;
                }
                var metric = new Metric();
                var cpu = os.getProcess(process.getProcessID()).getProcessCpuLoadCumulative();
                var memory = os.getProcess(process.getProcessID()).getResidentSetSize();
                var threadCount = os.getProcess(process.getProcessID()).getThreadCount();
                var timestamp = LocalDateTime.now();
                metric.setCpuUsage(cpu);
                metric.setMemoryUsage(memory);
                metric.setThreadCount(threadCount);
                metric.setTimeStamp(timestamp);
                metrics.computeIfAbsent(p, p1 -> {
                    var newMetrics = new ProcessMetrics();
                    newMetrics.setStartTime(p1.getOsProcess().getStartTime());
                    return newMetrics;
                }).addMetric(metric);
            });
        }, 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void onProcessKill(ProcessWrapper process) {
        observableProcesses.remove(process);
    }

    public void observeProcess(ProcessWrapper process) {
        observableProcesses.add(process);
        metrics.computeIfAbsent(process, _ -> new ProcessMetrics());
    }

    @PreDestroy
    private void destroy() {
        stopObserving();
    }
}
