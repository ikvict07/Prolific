package org.nevertouchgrass.prolific.service.configurations.creators;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.service.configurations.creators.contract.RunConfigurationCreator;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PythonRunConfigurationCreator implements RunConfigurationCreator<PythonRunConfigurationCreator.PythonConfigDescription> {
    private final UserSettingsHolder userSettingsHolder;
    private final NotificationService notificationService;
    private final LocalizationProvider localizationProvider;

    @Override
    public RunConfig createRunConfig(PythonConfigDescription description) {
        var runConfig = new RunConfig();
        var command = new ArrayList<String>();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        var pythonPath = userSettingsHolder.getPythonPath();
        var pythonCommand = isPython3Available() ? "python3" : "python";
        if (pythonPath.isBlank()) {
            notificationService.notifyError(ErrorNotification.of(null, localizationProvider.no_python_configured()));
            command.add(pythonCommand);
        } else {
            if (isWindows) {
                command.add(pythonPath+"/python.exe");
            } else {
                command.add(pythonPath+"/bin/python");
            }
        }

        command.add(description.getScriptPath());
        command.addAll(description.getArguments());

        runConfig.setCommand(command);
        runConfig.setType("Python");
        runConfig.setConfigName(description.getTitle());

        return runConfig;
    }

    @Data
    public static class PythonConfigDescription {
        String title;
        List<String> arguments;
        String scriptPath;
    }

    public boolean isPython3Available() {
        try {
            Process process;
            ProcessBuilder builder;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder = new ProcessBuilder("where", "python3");
            } else {
                builder = new ProcessBuilder("/bin/sh", "-c", "command -v python3");
            }
            process = builder.start();
            return process.waitFor() == 0;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Python3 check interrupted", e);
            return false;
        } catch (Exception e) {
            log.warn("Failed to check if python3 is available", e);
            return false;
        }
    }
}
