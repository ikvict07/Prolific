package org.nevertouchgrass.prolific.gradle.tasks;

import lombok.NonNull;
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

        File outputDir = new File(getProject().getProjectDir(), "src/main/java/");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File interfaceFile = new File(outputDir, packageName.replace('.', '/') + "/" + interfaceName +  ".java");
        interfaceFile.getParentFile().mkdirs();

        try (BufferedWriter writer = Files.newBufferedWriter(interfaceFile.toPath())) {
            writer.write("package " + packageName + ";\n\n");
            writer.write("import javafx.beans.property.StringProperty;\nimport javafx.beans.property.SimpleStringProperty;\n\n");
            writer.write("public interface " + interfaceName + " {\n");
            for (String key : properties.stringPropertyNames()) {
                writer.write("\tdefault StringProperty get" + normalize(key) + "Property() { return new SimpleStringProperty(\"" + key + "\"); }\n");
            }
            writer.write("}");
        }

        getLogger().lifecycle("Generated interface: " + interfaceFile.getAbsolutePath());
    }

    private String normalize(@NonNull String str) {
        str = str.trim();
        if (!str.isEmpty()) {
            str = str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        while (str.contains("_")) {
            int index = str.indexOf("_");
            str = str.substring(0, index) + str.substring(index + 1, index + 2).toUpperCase() + str.substring(index + 2);
        }

        return str.replace("_", "");
    }
}
