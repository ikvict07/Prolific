package org.nevertouchgrass.prolific.service.parser;

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

import static org.nevertouchgrass.prolific.constants.XmlConfigConstants.*;

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
        return name.equalsIgnoreCase(element.getAttribute(FACTORY_NAME));
    }

    public RunConfig extractRunConfig(Element config, String optionsAttribute) {
        RunConfig runConfig = new RunConfig();

        String configName = config.getAttribute(NAME);
        runConfig.setConfigName(configName);

        runConfig.setCommand(new ArrayList<>());
        NodeList options = config.getElementsByTagName(OPTION);

        for (int j = 0; j < options.getLength(); j++) {
            Element option = (Element) options.item(j);

            if (optionsAttribute.equalsIgnoreCase(option.getAttribute(NAME))) {
                addTaskNamesToConfig(option, runConfig);
            }
        }
        return runConfig;
    }

    public void addTaskNamesToConfig(Element option, RunConfig runConfig) {
        NodeList listOptions = option.getElementsByTagName(OPTION);
        for (int k = 0; k < listOptions.getLength(); k++) {
            Element taskOption = (Element) listOptions.item(k);
            String value = taskOption.getAttribute(VALUE);
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
