package org.nevertouchgrass.prolific.service.process;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.TerminatedProcessInfo;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.model.ProcessMetrics;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.service.runner.ProjectRunnerRegistry;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessService {
    private final Set<ProcessWrapper> live = ConcurrentHashMap.newKeySet();
    private final Set<ProcessWrapper> dead = ConcurrentHashMap.newKeySet();
    private final Set<Consumer<ProcessWrapper>> onKillListeners = ConcurrentHashMap.newKeySet();
    private final List<ProcessAware> processAware;
    private final ProjectRunnerRegistry projectRunner;
    private final ProcessLogsService processLogsService;
    private final MetricsService metricsService;

    private final Map<ProcessWrapper, Project> processToProject = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, RunConfig> processToRunConfig = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, LocalDateTime> processStartTimes = new ConcurrentHashMap<>();

    private final ObservableMap<Project, Set<ProcessWrapper>> observableProcessesMap = FXCollections.observableHashMap();

    // Use thread-safe deque for recent runs
    private final Deque<TerminatedProcessInfo> recentTerminatedRuns = new ConcurrentLinkedDeque<>();
    private static final int MAX_RECENT_RUNS = 10;

    public Set<ProcessWrapper> getLiveProcesses() {
        return Set.copyOf(live);
    }

    public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var process = projectRunner.runProject(project, runConfig);
        addProcess(project, process);

        // Track process info for terminated runs
        processToProject.put(process, project);
        processToRunConfig.put(process, runConfig);
        processStartTimes.put(process, LocalDateTime.now());

        process.getProcess().onExit().thenAccept(_ -> {
            onKillListeners.forEach(c -> c.accept(process));
            dead.add(process);
            log.debug("Process died: PID {} - {}", process.getPid(), process.getProcess().toString() // or .pid(), or another method, depending on your needs
            );

            // Record this as a terminated run
            Project proj = processToProject.get(process);
            RunConfig config = processToRunConfig.get(process);
            LocalDateTime startedAt = processStartTimes.get(process);
            LocalDateTime endedAt = LocalDateTime.now();
            int exitCode = process.getProcess().exitValue();

            ProcessLogs logs = processLogsService.getLogs().get(process);
            ProcessMetrics metrics = metricsService.getMetrics().get(process);

            recordTerminatedRun(new TerminatedProcessInfo(
                    proj, config, startedAt, endedAt, exitCode, logs, metrics
            ));
            System.out.println("Recorded recent run for project: " + (proj != null ? proj.getTitle() : "null"));

            // Clean up tracking maps
            processToProject.remove(process);
            processToRunConfig.remove(process);
            processStartTimes.remove(process);

            removeDeadProcess(process);
        });
        return process;
    }

    public boolean isProcessRunning(Project project) {
        var processes = observableProcessesMap.get(project);
        return processes != null && !processes.isEmpty();
    }

    public ObservableMap<Project, Set<ProcessWrapper>> getObservableLiveProcesses() {
        return observableProcessesMap;
    }

    public void addProcess(Project project, ProcessWrapper process) {
        if (live.add(process)) {
            Set<ProcessWrapper> value = observableProcessesMap.getOrDefault(project, ConcurrentHashMap.newKeySet());
            value.add(process);
            observableProcessesMap.put(project, value);
        }
    }

    @PostConstruct
    public void init() {
        onKillListeners.addAll(processAware.stream().map(pa -> (Consumer<ProcessWrapper>) pa::onProcessKill).toList());
    }

    public void registerOnKillListener(Consumer<ProcessWrapper> listener) {
        onKillListeners.add(listener);
    }

    private void removeDeadProcess(ProcessWrapper toRemove) {
        live.remove(toRemove);
        for (Map.Entry<Project, Set<ProcessWrapper>> entry : observableProcessesMap.entrySet()) {
            Set<ProcessWrapper> processes = entry.getValue();
            if (processes.isEmpty()) {
                observableProcessesMap.remove(entry.getKey());
            }
            if (processes.remove(toRemove)) {
                if (processes.isEmpty()) {
                    observableProcessesMap.remove(entry.getKey());
                }
                break;
            }
        }
        if (dead.size() > 1000) {
            AtomicInteger toRemoveCount = new AtomicInteger(dead.size() - 500);
            dead.removeIf(_ -> toRemoveCount.getAndDecrement() > 0);
        }
        log.debug("Removed {} dead process from tracking", toRemove.getName());
    }

    @PreDestroy
    public void destroy() {
        live.clear();
        dead.clear();
        onKillListeners.clear();
        observableProcessesMap.clear();
    }

    public List<TerminatedProcessInfo> getRecentTerminatedRuns() {
        return List.copyOf(recentTerminatedRuns);
    }

    public void recordTerminatedRun(TerminatedProcessInfo info) {
        System.out.println("Recording terminated run: " + info);
        if (recentTerminatedRuns.size() >= MAX_RECENT_RUNS) {
            recentTerminatedRuns.removeFirst();
        }
        recentTerminatedRuns.addLast(info);
    }
}
