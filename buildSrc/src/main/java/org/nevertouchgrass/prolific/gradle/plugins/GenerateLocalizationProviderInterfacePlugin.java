package org.nevertouchgrass.prolific.gradle.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.nevertouchgrass.prolific.gradle.tasks.GenerateLocalizationProviderInterfaceTask;

import java.io.File;

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
            task.setBuildDir(extension.getOutputDir());
        });


        project.afterEvaluate(p -> {
            File generatedFile = new File(extension.getOutputDir());
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            SourceSet main = sourceSets.getByName("main");
            main.getJava().srcDir(generatedFile);
        });

        project.getTasks().named("compileJava", JavaCompile.class).configure(task -> task.dependsOn(taskName));
    }
}
