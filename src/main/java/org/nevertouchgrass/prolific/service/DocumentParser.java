package org.nevertouchgrass.prolific.service;

import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.model.RunConfig;
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
public class DocumentParser {
    @SneakyThrows
    public Document parseXmlDocument(Path path) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(path.toFile());
        document.getDocumentElement().normalize();
        return document;
    }


    public boolean isConfigFor(String name, Element element) {
        return name.equalsIgnoreCase(element.getAttribute("factoryName"));
    }

    public RunConfig extractRunConfig(Element config, String optionsAttribute) {
        RunConfig runConfig = new RunConfig();

        String configName = config.getAttribute("name");
        runConfig.setConfigName(configName);

        runConfig.setCommand(new ArrayList<>());
        NodeList options = config.getElementsByTagName("option");

        for (int j = 0; j < options.getLength(); j++) {
            Element option = (Element) options.item(j);

            if (optionsAttribute.equalsIgnoreCase(option.getAttribute("name"))) {
                addTaskNamesToConfig(option, runConfig);
            }
        }
        return runConfig;
    }

    public void addTaskNamesToConfig(Element option, RunConfig runConfig) {
        NodeList listOptions = option.getElementsByTagName("option");
        for (int k = 0; k < listOptions.getLength(); k++) {
            Element taskOption = (Element) listOptions.item(k);
            String value = taskOption.getAttribute("value");
            if (!value.isEmpty()) {
                runConfig.getCommand().add(value);
            }
        }
    }

    public List<RunConfig> getNamedRunConfigs(NodeList configurations, String projectType, String optionsAttribute) {
        List<RunConfig> configs = new ArrayList<>();
        for (int i = 0; i < configurations.getLength(); i++) {
            Element config = (Element) configurations.item(i);
            if (isConfigFor(projectType, config)) {
                RunConfig runConfig = extractRunConfig(config, optionsAttribute);
                if (!runConfig.getCommand().isEmpty()) {
                    configs.add(runConfig);
                }
            }
        }
        return configs;
    }
}
