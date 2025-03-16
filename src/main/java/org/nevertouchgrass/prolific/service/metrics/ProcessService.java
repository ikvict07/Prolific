package org.nevertouchgrass.prolific.service.metrics;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Service;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProcessService {
    private final OperatingSystem os;
    private final List<OSProcess> live = new CopyOnWriteArrayList<>();
    private final List<OSProcess> dead = new CopyOnWriteArrayList<>();
    private final List<Consumer<OSProcess>> onKillListeners = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private volatile boolean observing = true;


    public List<OSProcess> getLiveProcesses() {
        return List.copyOf(live);
    }

    @PostConstruct
    private void init() {
        scheduleProcessObserving();
    }

    public List<OSProcess> getRunningProcesses(Project project) {
        var processes = os.getProcesses();
        var result = processes.stream().filter(p -> p.getCommandLine().contains(project.getPath())).toList();
        if (!result.isEmpty()) {
            log.debug("For project {} found {} running processes", project.getPath(), result.size());
            log.trace("{} Running processes: {}", project, result);
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

    public void stopObserving() {
        observing = false;
        executorService.shutdown();
    }

    public void scheduleProcessObserving() {
        executorService.scheduleAtFixedRate(() -> {
            while (observing) {
                var toRemove = new ArrayList<OSProcess>();
                live.forEach(p -> {
                    if (os.getProcess(p.getProcessID()) == null) {
                        dead.add(p);
                        onKillListeners.forEach(c -> c.accept(p));
                        toRemove.add(p);
                    }
                });
                live.removeAll(toRemove);
            }
        }, 0, 3, TimeUnit.SECONDS);
    }


    @PreDestroy
    private void destroy() {
        stopObserving();
    }
}
