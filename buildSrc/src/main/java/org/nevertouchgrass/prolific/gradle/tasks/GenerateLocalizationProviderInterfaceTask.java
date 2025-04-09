package org.nevertouchgrass.prolific.gradle.tasks;

import lombok.Setter;
import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Setter
public class GenerateLocalizationProviderInterfaceTask extends DefaultTask {
    private String resourceFile;
    private String packageName;
    private String interfaceName;
    private String buildDir;

    public GenerateLocalizationProviderInterfaceTask() {
        setGroup("build");
        setDescription("Generates the LocalizationProvider interface from the resource file.");
    }

    @TaskAction
    @SneakyThrows
    public void generateInterface() {
        Properties properties = new Properties();

        try (InputStream inputStream = new FileInputStream(new File(getProject().getProjectDir(), resourceFile))) {
            properties.loadFromXML(inputStream);
        }

        File outputDir = new File(getProject().getProjectDir(), buildDir);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File interfaceFile = new File(outputDir, packageName.replace('.', '/') + "/" + interfaceName +  ".java");
        interfaceFile.getParentFile().mkdirs();

        try (BufferedWriter writer = Files.newBufferedWriter(interfaceFile.toPath())) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("import javafx.beans.property.StringProperty;\n\n");
            writer.write("public interface " + interfaceName + " {\n");
            for (String key : properties.stringPropertyNames()) {
                writer.write("\tStringProperty " + key + "();\n");
            }
            writer.write("}");
        }

        getLogger().lifecycle("Generated interface: " + interfaceFile.getAbsolutePath());
    }
}
