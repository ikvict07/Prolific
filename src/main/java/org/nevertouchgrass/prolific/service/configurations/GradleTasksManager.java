package org.nevertouchgrass.prolific.service.configurations;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

@Service
public class GradleTasksManager {
    public List<String> getGradleTasks(Path projectPath) {
        var processBuilder = new ProcessBuilder();
        final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            processBuilder.command("cmd.exe", "/c", "gradlew.bat", "tasks", "--all");
        } else {
            processBuilder.command("./gradlew", "tasks", "--all");
        }
        processBuilder.directory(projectPath.toFile());
        try {
            var process = processBuilder.start();
            var output = process.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(output));
            var tasks = reader.lines().toList();
            return tasks.stream().filter(this::filterGradleTasks).map(t -> t.split("\\s-\\s")[0]).toList();
        } catch (Exception e) {
            return List.of();
        }
    }


    private boolean filterGradleTasks(String line) {
        if (line.isBlank() || line.matches("^-+$")) return false;

        if (line.startsWith("> Task") || line.startsWith("Tasks runnable")
            || line.startsWith("Build tasks")
            || line.startsWith("Build Setup tasks")
            || line.startsWith("Verification tasks")
            || line.startsWith("Documentation tasks")
            || line.startsWith("Help tasks")
            || line.startsWith("Rules") || line.startsWith("Pattern:")) return false;

        return line.matches("^[a-zA-Z0-9_-]+(\\s+-\\s+.*)?$");
    }
}
