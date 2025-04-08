package org.nevertouchgrass.prolific.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.nevertouchgrass.prolific.gradle.tasks.GenerateLocalizationProviderInterfaceTask;

@SuppressWarnings("unused")
public class GenerateLocalizationProviderInterfacePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        String taskName = "generateLocalizationProviderInterface";
        GenerateLocalizationProviderInterfacePluginExtension extension = project.getExtensions().create(taskName, GenerateLocalizationProviderInterfacePluginExtension.class);
        project.getTasks().register(taskName, GenerateLocalizationProviderInterfaceTask.class, task -> {
            task.setResourceFile(extension.getResourceFile());
            task.setPackageName(extension.getPackageName());
            task.setInterfaceName(extension.getInterfaceName());
        });

        project.getTasks().named("compileJava", task -> task.dependsOn(taskName));
    }
}
