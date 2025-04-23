package org.nevertouchgrass.prolific.service.configurations.creators;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.service.configurations.creators.contract.RunConfigurationCreator;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnacondaRunConfigurationCreator implements RunConfigurationCreator<AnacondaRunConfigurationCreator.AnacondaDescription> {
    private final UserSettingsHolder userSettingsHolder;
    private final NotificationService notificationService;
    private final LocalizationProvider localizationProvider;

    @Override
    public RunConfig createRunConfig(AnacondaDescription description) {
        var runConfig = new RunConfig();
        var command = new ArrayList<String>();
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        var condaPath = userSettingsHolder.getAnacondaPath();
        if (condaPath.isBlank()) {
            notificationService.notifyError(ErrorNotification.of(null, localizationProvider.no_anaconda_configured()));
            condaPath = "%{path}%";
        }
        if (isWindows) {
            command.add(condaPath+"/envs/"+description.getEnvironmentName()+"/python.exe");
        } else {
            command.add(condaPath+"/envs/"+description.getEnvironmentName()+"/bin/python");
        }
        command.add(description.getScriptPath());
        command.addAll(description.getScriptArgs());
        runConfig.setCommand(command);
        runConfig.setType("Anaconda");
        runConfig.setConfigName(description.getTitle());
        return runConfig;
    }

    @Data
    public static class AnacondaDescription {
        String title;
        String environmentName;
        String scriptPath;
        List<String> scriptArgs;
    }
}
