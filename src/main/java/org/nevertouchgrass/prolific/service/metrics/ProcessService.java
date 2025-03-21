package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Service;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.List;
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
    private final Set<OSProcess> live = ConcurrentHashMap.newKeySet();
    private final Set<OSProcess> dead = ConcurrentHashMap.newKeySet();
    private final Set<Consumer<OSProcess>> onKillListeners = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean observing = true;
    private final List<ProcessAware> processAware;


    public List<OSProcess> getLiveProcesses() {
        return List.copyOf(live);
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
    public List<OSProcess> getRunningProcesses(Project project) {
        var processes = os.getProcesses();
        // Create pattern only once per method call
        var patternString = "(^|/)" + Pattern.quote(project.getTitle()) + "(\\s|$|/|])";
        var pattern = Pattern.compile(patternString);

        var result = processes.stream()
                .filter(p -> p.getCommandLine().contains(project.getPath()))
                .filter(p -> pattern.matcher(p.getCommandLine()).find())
                .toList();

        if (!result.isEmpty()) {
            log.info("For project {} found {} running processes", project.getPath(), result.size());
            log.info("{} Running processes: {}", project, result.stream().map(OSProcess::getCommandLine).toList());
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
        var processes = getRunningProcesses(project);
        live.addAll(processes);
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
        // Create a thread factory for better thread naming
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

        // Replace the executor service with one that uses the named thread factory
        if (executorService instanceof ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
            scheduledThreadPoolExecutor.setThreadFactory(threadFactory);
        }

        executorService.scheduleAtFixedRate(() -> {
            if (!observing) {
                return;
            }

            // Use CopyOnWriteArrayList for thread safety
            List<OSProcess> toRemove = new CopyOnWriteArrayList<>();

            live.forEach(p -> {
                if (os.getProcess(p.getProcessID()) == null) {
                    dead.add(p);
                    onKillListeners.forEach(c -> c.accept(p));
                    toRemove.add(p);
                }
            });

            // Remove dead processes from the live set
            if (!toRemove.isEmpty()) {
                live.removeAll(toRemove);

                // Limit the size of the dead set to prevent memory leaks
                if (dead.size() > 1000) {
                    // Keep only the most recent 500 dead processes
                    dead.removeIf(p -> !toRemove.contains(p) && dead.size() > 500);
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
    }
}
