package org.nevertouchgrass.prolific.service.xml;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.xml.contract.ConfigImporter;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class GradleConfigImporter implements ConfigImporter {
    @Override
    public List<RunConfig> importConfig(Project project) {
        var workspacePath = getWorkspacePath(project);
        if (!workspacePath.toFile().exists()) {
            return List.of();
        }
        try {
            Document document = parseXmlDocument(workspacePath);
            NodeList configurations = document.getElementsByTagName("configuration");

            return getGradleRunConfigs(configurations);
        } catch (Exception e) {
            log.error("Failed to import run configurations for project {}", project.getTitle(), e);
            return List.of();
        }
    }

    @Override
    public boolean supports(Project project) {
        return project.getType().equalsIgnoreCase("gradle");
    }

    private Path getWorkspacePath(Project project) {
        return Path.of(project.getPath()).resolve(".idea").resolve("workspace.xml");
    }

    @SneakyThrows
    private Document parseXmlDocument(Path path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(path.toFile());
        document.getDocumentElement().normalize();
        return document;
    }

    private List<RunConfig> getGradleRunConfigs(NodeList configurations) {
        List<RunConfig> configs = new ArrayList<>();

        for (int i = 0; i < configurations.getLength(); i++) {
            Element config = (Element) configurations.item(i);
            if (isGradleConfig(config)) {
                RunConfig runConfig = extractRunConfig(config);
                if (!runConfig.getCommand().isEmpty()) {
                    configs.add(runConfig);
                }
            }
        }

        configs.forEach(this::normalize);
        return configs;
    }

    public void normalize(RunConfig runConfig) {
        runConfig.getCommand().addFirst("./gradlew");
        runConfig.setType("gradle");
    }

    private boolean isGradleConfig(Element config) {
        return "Gradle".equals(config.getAttribute("factoryName"));
    }

    private RunConfig extractRunConfig(Element config) {
        RunConfig runConfig = new RunConfig();

        String configName = config.getAttribute("name");
        runConfig.setConfigName(configName);

        runConfig.setCommand(new ArrayList<>());
        NodeList options = config.getElementsByTagName("option");

        for (int j = 0; j < options.getLength(); j++) {
            Element option = (Element) options.item(j);

            if ("taskNames".equals(option.getAttribute("name"))) {
                addTaskNamesToConfig(option, runConfig);
            }
        }
        return runConfig;
    }

    private void addTaskNamesToConfig(Element option, RunConfig runConfig) {
        NodeList listOptions = option.getElementsByTagName("option");
        for (int k = 0; k < listOptions.getLength(); k++) {
            Element taskOption = (Element) listOptions.item(k);
            String value = taskOption.getAttribute("value");
            if (!value.isEmpty()) {
                runConfig.getCommand().add(value);
            }
        }
    }
}