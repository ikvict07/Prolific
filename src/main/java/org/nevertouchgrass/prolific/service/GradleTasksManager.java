package org.nevertouchgrass.prolific.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
public class GradleTasksManager {
    public List<String> getGradleTasks(Path projectPath) {
        var processBuilder = new ProcessBuilder();
        processBuilder.command("./gradlew", "tasks");
        processBuilder.directory(projectPath.toFile());
        try {
            var process = processBuilder.start();
            var output = process.getInputStream();
            var reader = new java.io.BufferedReader(new java.io.InputStreamReader(output));
            var tasks = reader.lines().toList();
            return tasks.stream().filter(this::filterGradleTasks).map((t) -> t.split("\\s-\\s")[0]).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean filterGradleTasks(String line) {
        if (line.matches("-*")) {
            return false;
        }
        if (line.startsWith("Tasks runnable")) {
            return false;
        }
        if (line.isBlank()) {
            return false;
        }
        return line.contains(" - ");
    }
}
