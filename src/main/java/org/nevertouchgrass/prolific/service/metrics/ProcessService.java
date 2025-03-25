package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.util.OSProcessWrapper;
import org.springframework.stereotype.Service;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessService {
    private final OperatingSystem os;
    private final Set<OSProcessWrapper> live = ConcurrentHashMap.newKeySet();
    private final Set<OSProcessWrapper> dead = ConcurrentHashMap.newKeySet();
    private final Set<Consumer<OSProcess>> onKillListeners = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean observing = true;
    private final List<ProcessAware> processAware;
    private final Set<Project> observedProjects = ConcurrentHashMap.newKeySet();
    private final Set<OSProcess> processes = ConcurrentHashMap.newKeySet();

    private final ObservableMap<Project, Set<OSProcessWrapper>> observableProcessesMap = FXCollections.observableHashMap();

    public List<OSProcessWrapper> getLiveProcesses() {
        return List.copyOf(live);
    }

    public ObservableMap<Project, Set<OSProcessWrapper>> getObservableLiveProcesses() {
        return observableProcessesMap;
    }

    public void addProcess(long pid, Project project) {
        OSProcessWrapper process = new OSProcessWrapper(os.getProcess((int) pid));
        if (live.add(process)) {
            Set<OSProcessWrapper> value = observableProcessesMap.getOrDefault(project, ConcurrentHashMap.newKeySet());
            value.add(process);
            observableProcessesMap.put(project, value);
            log.debug("New process detected: PID {} - {}", process.getProcess().getProcessID(), process.getProcess().getName());
        }
    }

    /**
     * Initializes the service by scheduling process observation and registering process-aware listeners.
     */
    @PostConstruct
    public void init() {
        scheduleProcessObserving();
        onKillListeners.addAll(processAware.stream().map(pa -> (Consumer<OSProcess>) pa::onProcessKill).toList());
    }

    /**
     * Gets a list of running processes for a project.
     * Uses a cached pattern to improve performance.
     *
     * @param project the project to get running processes for
     * @return a list of running processes for the project
     */
    public List<OSProcessWrapper> getRunningProcesses(Project project) {
        // Create pattern only once per method call
        var patternString = "(^|/)" + Pattern.quote(project.getTitle()) + "(\\s|$|/|])";
        var pattern = Pattern.compile(patternString);

        var result = processes.stream()
                .filter(p -> p.getCommandLine().contains(project.getPath()))
                .filter(p -> pattern.matcher(p.getCommandLine()).find())
                .map(OSProcessWrapper::new)
                .toList();

        if (!result.isEmpty()) {
            log.info("For project {} found {} running processes", project.getPath(), result.size());
            log.info("{} Running processes: {}", project, result.stream().map(it -> it.getProcess().getCommandLine()).toList());
        }
        return result;
    }

    public OSProcess getProcessById(int pid) {
        return os.getProcess(pid);
    }

    public OSProcess osProcessFromProcess(Process process) {
        return getProcessById((int) process.pid());
    }

    public void registerOnKillListener(Consumer<OSProcess> listener) {
        onKillListeners.add(listener);
    }

    public void observe(Project project) {
        if (project.getPath().endsWith(System.getProperty("user.home"))){
            return;
        }
        synchronized (processes) {
            processes.clear();
            processes.addAll(os.getProcesses());
        }
        observedProjects.add(project);

        var processes = getRunningProcesses(project);
        if (live.addAll(processes)) {
            Set<OSProcessWrapper> value = ConcurrentHashMap.newKeySet();
            value.addAll(processes);
            observableProcessesMap.put(project, value);
        }

        log.info("Started observing project: {}. Initial processes: {}",
                project.getTitle(), processes.size());
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
            public Thread newThread(Runnable r) {
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
            processes.clear();
            processes.addAll(os.getProcesses());

            for (Project project : observedProjects) {
                List<OSProcessWrapper> currentProcesses = getRunningProcesses(project);

                Set<OSProcessWrapper> processes = observableProcessesMap.getOrDefault(project, ConcurrentHashMap.newKeySet());

                for (OSProcessWrapper process : currentProcesses) {
                    if (live.add(process)) {
                            processes.add(process);
                            observableProcessesMap.put(project, processes);
                        log.debug("New process detected for project {}: PID {} - {}",
                                project.getTitle(), process.getProcess().getProcessID(), process.getProcess().getName());
                    }
                }
            }

            List<OSProcessWrapper> toRemove = new CopyOnWriteArrayList<>();

            live.forEach(p -> {
                if (os.getProcess(p.getProcess().getProcessID()) == null) {
                    dead.add(p);
                    log.debug("Process died: PID {} - {}", p.getProcess().getProcessID(), p.getProcess().getName());
                    onKillListeners.forEach(c -> {
                        try {
                            c.accept(p.getProcess());
                        } catch (Exception e) {
                            log.error("Error in process kill listener", e);
                        }
                    });
                    toRemove.add(p);
                }
            });

            if (!toRemove.isEmpty()) {
                toRemove.forEach(p -> {
                    live.remove(p);
                    for (Map.Entry<Project, Set<OSProcessWrapper>> entry : observableProcessesMap.entrySet()) {
                        Set<OSProcessWrapper> processes = entry.getValue();
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
                log.debug("Removed {} dead processes from tracking", toRemove.size());

                if (dead.size() > 1000) {
                    AtomicInteger toRemoveCount = new AtomicInteger(dead.size() - 500);
                    dead.removeIf(p -> !toRemove.contains(p) && toRemoveCount.getAndDecrement() > 0);
                }
            }
        }, 0, 3, TimeUnit.SECONDS);
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
