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
import org.nevertouchgrass.prolific.service.runner.DefaultProjectRunner;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final DefaultProjectRunner projectRunner;


    public ProcessWrapper runProject(Project project, RunConfig runConfig) throws ProcessStartFailedException {
        var process = projectRunner.runProject(project, runConfig);
        addProcess(project, process);
        process.getProcess().onExit().thenAccept(_ -> {
            onKillListeners.forEach(c -> c.accept(process));
            dead.add(process);
            log.debug("Process died: PID {} - {}", process.getPid(), process.getOsProcess().getName());
            removeDeadProcess(process);
        });
        return process;
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
            log.debug("New process detected: PID {} - {}", process.getPid(), process.getOsProcess().getName());
        }
    }

    /**
     * Initializes the service by scheduling process observation and registering process-aware listeners.
     */
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

    /**
     * Cleans up resources when the service is being destroyed.
     */
    @PreDestroy
    public void destroy() {
        // Clear collections to help with garbage collection
        live.clear();
        dead.clear();
        onKillListeners.clear();
        observableProcessesMap.clear();
    }
}
