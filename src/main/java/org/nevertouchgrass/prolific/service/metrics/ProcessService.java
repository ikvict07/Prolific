package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessService {
    private final OperatingSystem os;
    private final Set<ProcessWrapper> live = ConcurrentHashMap.newKeySet();
    private final Set<ProcessWrapper> dead = ConcurrentHashMap.newKeySet();
    private final Set<Consumer<ProcessWrapper>> onKillListeners = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean observing = true;
    private final List<ProcessAware> processAware;
    private final Set<Project> observedProjects = ConcurrentHashMap.newKeySet();

    private final ObservableMap<Project, Set<ProcessWrapper>> observableProcessesMap = FXCollections.observableHashMap();

    public ObservableMap<Project, Set<ProcessWrapper>> getObservableLiveProcesses() {
        return observableProcessesMap;
    }

    public void addProcess(Project project, ProcessWrapper process) {
        if (live.add(process)) {
            Set<ProcessWrapper> value = observableProcessesMap.getOrDefault(project, ConcurrentHashMap.newKeySet());
            value.add(process);
            observableProcessesMap.put(project, value);
            log.debug("New process detected: PID {} - {}", process.getOsProcess().getProcessID(), process.getOsProcess().getName());
        }
    }

    /**
     * Initializes the service by scheduling process observation and registering process-aware listeners.
     */
    @PostConstruct
    public void init() {
        scheduleProcessObserving();
        onKillListeners.addAll(processAware.stream().map(pa -> (Consumer<ProcessWrapper>) pa::onProcessKill).toList());
    }

    public void registerOnKillListener(Consumer<ProcessWrapper> listener) {
        onKillListeners.add(listener);
    }

    public void stopObserve(Project project) {
        observedProjects.remove(project);
        log.info("Stopped observing project: {}", project.getTitle());
    }

    /**
     * Stops observing processes and cleans up resources.
     * This method is called when the application is shutting down or when observation is no longer needed.
     */
    public void stopObserving() {
        observing = false;

        // Shutdown the executor service gracefully
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
            log.error("Error shutting down executor service", e);
        }
    }

    /**
     * Schedules a task to periodically check if observed processes are still running.
     * If a process is no longer running, it notifies listeners and removes it from the live set.
     */
    public void scheduleProcessObserving() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(@NonNull Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setName("process-service-" + threadNumber.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };

        if (executorService instanceof ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
            scheduledThreadPoolExecutor.setThreadFactory(threadFactory);
        }

        executorService.scheduleAtFixedRate(() -> {
            if (!observing) {
                return;
            }
            List<ProcessWrapper> toRemove = new CopyOnWriteArrayList<>();

            live.forEach(p -> detectDeadProcesses(p, toRemove));
            if (!toRemove.isEmpty()) {
                removeDeadProjects(toRemove);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void removeDeadProjects(List<ProcessWrapper> toRemove) {
        toRemove.forEach(p -> {
            live.remove(p);
            for (Map.Entry<Project, Set<ProcessWrapper>> entry : observableProcessesMap.entrySet()) {
                Set<ProcessWrapper> processes = entry.getValue();
                if (processes.isEmpty()) {
                    observableProcessesMap.remove(entry.getKey());
                }
                if (processes.remove(p)) {
                    if (processes.isEmpty()) {
                        observableProcessesMap.remove(entry.getKey());
                    }
                    break;
                }
            }
        });
        if (dead.size() > 1000) {
            AtomicInteger toRemoveCount = new AtomicInteger(dead.size() - 500);
            dead.removeIf(p -> !toRemove.contains(p) && toRemoveCount.getAndDecrement() > 0);
        }
        log.debug("Removed {} dead processes from tracking", toRemove.size());
    }

    private void detectDeadProcesses(ProcessWrapper p, List<ProcessWrapper> toRemove) {
        if (os.getProcess(p.getOsProcess().getProcessID()) == null) {
            dead.add(p);
            log.debug("Process died: PID {} - {}", p.getOsProcess().getProcessID(), p.getOsProcess().getName());
            onKillListeners.forEach(c -> {
                try {
                    c.accept(p);
                } catch (Exception e) {
                    log.error("Error in process kill listener", e);
                }
            });
            toRemove.add(p);
        }
    }


    /**
     * Cleans up resources when the service is being destroyed.
     */
    @PreDestroy
    public void destroy() {
        stopObserving();
        // Clear collections to help with garbage collection
        live.clear();
        dead.clear();
        onKillListeners.clear();
        observableProcessesMap.clear();
    }
}
