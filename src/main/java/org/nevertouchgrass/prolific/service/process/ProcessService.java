//package org.nevertouchgrass.prolific.service.process;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableMap;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.log4j.Log4j2;
//import org.nevertouchgrass.prolific.exception.ProcessStartFailedException;
//import org.nevertouchgrass.prolific.model.Project;
//import org.nevertouchgrass.prolific.model.RunConfig;
//import org.nevertouchgrass.prolific.model.TerminatedProcessInfo;
//import org.nevertouchgrass.prolific.service.runner.DefaultProjectRunner;
//import org.nevertouchgrass.prolific.util.ProcessWrapper;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Consumer;
//
//@Service
//@RequiredArgsConstructor
//@Log4j2
//public class ProcessService {
//    private final Set<ProcessWrapper> live = ConcurrentHashMap.newKeySet();
//    private final Set<ProcessWrapper> dead = ConcurrentHashMap.newKeySet();
//    private final Set<Consumer<ProcessWrapper>> onKillListeners = ConcurrentHashMap.newKeySet();
//    private final List<ProcessAware> processAware;
//    private final DefaultProjectRunner projectRunner;
//
//    public Set<ProcessWrapper> getLiveProcesses() {
//        return Set.copyOf(live);
//    }
//    public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
//        var process = projectRunner.runProject(project, runConfig);
//        addProcess(project, process);
//        process.getProcess().onExit().thenAccept(_ -> {
//            onKillListeners.forEach(c -> c.accept(process));
//            dead.add(process);
//            log.debug("Process died: PID {} - {}", process.getPid(), process.getOsProcess().getName());
//            removeDeadProcess(process);
//        });
//        return process;
//    }
//
//    public boolean isProcessRunning(Project project) {
//        var processes = observableProcessesMap.get(project);
//        if (processes == null) {
//            return false;
//        }
//        return !processes.isEmpty();
//    }
//
//    private final ObservableMap<Project, Set<ProcessWrapper>> observableProcessesMap = FXCollections.observableHashMap();
//
//    public ObservableMap<Project, Set<ProcessWrapper>> getObservableLiveProcesses() {
//        return observableProcessesMap;
//    }
//
//    public void addProcess(Project project, ProcessWrapper process) {
//        if (live.add(process)) {
//            Set<ProcessWrapper> value = observableProcessesMap.getOrDefault(project, ConcurrentHashMap.newKeySet());
//            value.add(process);
//            observableProcessesMap.put(project, value);
//            log.debug("New process detected: PID {} - {}", process.getPid(), process.getOsProcess().getName());
//        }
//    }
//
//    /**
//     * Initializes the service by scheduling process observation and registering process-aware listeners.
//     */
//    @PostConstruct
//    public void init() {
//        onKillListeners.addAll(processAware.stream().map(pa -> (Consumer<ProcessWrapper>) pa::onProcessKill).toList());
//    }
//
//    public void registerOnKillListener(Consumer<ProcessWrapper> listener) {
//        onKillListeners.add(listener);
//    }
//
//    private void removeDeadProcess(ProcessWrapper toRemove) {
//        live.remove(toRemove);
//        for (Map.Entry<Project, Set<ProcessWrapper>> entry : observableProcessesMap.entrySet()) {
//            Set<ProcessWrapper> processes = entry.getValue();
//            if (processes.isEmpty()) {
//                observableProcessesMap.remove(entry.getKey());
//            }
//            if (processes.remove(toRemove)) {
//                if (processes.isEmpty()) {
//                    observableProcessesMap.remove(entry.getKey());
//                }
//                break;
//            }
//        }
//        if (dead.size() > 1000) {
//            AtomicInteger toRemoveCount = new AtomicInteger(dead.size() - 500);
//            dead.removeIf(_ -> toRemoveCount.getAndDecrement() > 0);
//        }
//        log.debug("Removed {} dead process from tracking", toRemove.getName());
//    }
//
//    /**
//     * Cleans up resources when the service is being destroyed.
//     */
//    @PreDestroy
//    public void destroy() {
//        // Clear collections to help with garbage collection
//        live.clear();
//        dead.clear();
//        onKillListeners.clear();
//        observableProcessesMap.clear();
//    }
//
//    private final Deque<TerminatedProcessInfo> recentTerminatedRuns = new ArrayDeque<>();
//
//    public List<TerminatedProcessInfo> getRecentTerminatedRuns() {
//        return List.copyOf(recentTerminatedRuns);
//    }
//
//    private static final int MAX_RECENT_RUNS = 10;
//
//    public void recordTerminatedRun(TerminatedProcessInfo info) {
//        if (recentTerminatedRuns.size() >= MAX_RECENT_RUNS) {
//            recentTerminatedRuns.removeFirst();
//        }
//        recentTerminatedRuns.addLast(info);
//    }
//
//
//}


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
<<<<<<< HEAD
import org.nevertouchgrass.prolific.model.TerminatedProcessInfo;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.nevertouchgrass.prolific.model.ProcessMetrics;
import org.nevertouchgrass.prolific.service.logging.ProcessLogsService;   // <-- NEW
import org.nevertouchgrass.prolific.service.metrics.MetricsService;      // <-- NEW
import org.nevertouchgrass.prolific.service.runner.DefaultProjectRunner;
=======
import org.nevertouchgrass.prolific.service.runner.ProjectRunnerRegistry;
>>>>>>> upstream/windows-support
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;  // <-- NEW
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    // --- NEW: Inject services to access logs & metrics ---
    private final ProcessLogsService processLogsService;
    private final MetricsService metricsService;

    // --- NEW: Track process info for terminated runs ---
    private final Map<ProcessWrapper, Project> processToProject = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, RunConfig> processToRunConfig = new ConcurrentHashMap<>();
    private final Map<ProcessWrapper, LocalDateTime> processStartTimes = new ConcurrentHashMap<>();

    public Set<ProcessWrapper> getLiveProcesses() {
        return Set.copyOf(live);
    }

    public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var process = projectRunner.runProject(project, runConfig);
        addProcess(project, process);

        // --- NEW: Remember info for this process ---
        processToProject.put(process, project);
        processToRunConfig.put(process, runConfig);
        processStartTimes.put(process, LocalDateTime.now());

        process.getProcess().onExit().thenAccept(_ -> {
            onKillListeners.forEach(c -> c.accept(process));
            dead.add(process);
<<<<<<< HEAD
            log.debug("Process died: PID {} - {}", process.getPid(), process.getOsProcess().getName());

            // --- NEW: Record this as a terminated run ---
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
            //debug
            System.out.println("Recorded recent run for project: " + proj.getTitle());

            // --- NEW: Clean up the maps ---
            processToProject.remove(process);
            processToRunConfig.remove(process);
            processStartTimes.remove(process);


=======
>>>>>>> upstream/windows-support
            removeDeadProcess(process);
        });
        return process;
    }

    public boolean isProcessRunning(Project project) {
        var processes = observableProcessesMap.get(project);
        if (processes == null) {
            return false;
        }
        return !processes.isEmpty();
    }

    private final ObservableMap<Project, Set<ProcessWrapper>> observableProcessesMap = FXCollections.observableHashMap();

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

    // --- NEW: Track recent terminated runs ---
   // private final Deque<TerminatedProcessInfo> recentTerminatedRuns = new ArrayDeque<>();
    private final Deque<TerminatedProcessInfo> recentTerminatedRuns = new java.util.concurrent.ConcurrentLinkedDeque<>();

    private static final int MAX_RECENT_RUNS = 10;

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
