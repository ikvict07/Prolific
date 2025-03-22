package org.nevertouchgrass.prolific.service;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nevertouchgrass.prolific.configuration.PluginConfigProvider;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.nevertouchgrass.prolific.constants.XmlConfigConstants.FILE;
import static org.nevertouchgrass.prolific.constants.XmlConfigConstants.FOLDER;
import static org.nevertouchgrass.prolific.constants.XmlConfigConstants.NAME;
import static org.nevertouchgrass.prolific.constants.XmlConfigConstants.PROJECT;

/**
 * Service for loading user's plugins, that will be used in a project finding process
 */
@Service
@Log4j2
public class XmlProjectScannerConfigLoaderService {

    private static final Logger LOGGER = LogManager.getLogger();

    private final PluginConfigProvider pluginConfigProvider;

    public XmlProjectScannerConfigLoaderService(PluginConfigProvider pluginConfigProvider) {
        this.pluginConfigProvider = pluginConfigProvider;
    }

    public List<ProjectTypeModel> loadProjectTypes() {
        Path path = pluginConfigProvider.getPluginConfigPath();
        if (path == null) {
            log.warn("Plugin configuration file does not exist");
            return new ArrayList<>();
        }
        List<ProjectTypeModel> projectTypeModels = new ArrayList<>();

        try (var file = Files.newInputStream(path)) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);

            document.getDocumentElement().normalize();

            NodeList projectTypeNodes = document.getElementsByTagName(PROJECT);

            for (int i = 0; i < projectTypeNodes.getLength(); i++) {
                Element projectTypeElement = (Element) projectTypeNodes.item(i);
                String projectName = projectTypeElement.getElementsByTagName(NAME).item(0).getTextContent();
                List<String> identifiers = getIdentifiers(projectTypeElement);

                projectTypeModels.add(new ProjectTypeModel(projectName, identifiers));
            }
        } catch (Exception e) {
            LOGGER.error("Error loading plugins from XML file", e);
        }

        return projectTypeModels;
    }

    private static List<String> getIdentifiers(Element projectTypeElement) {
        List<String> identifiers = new ArrayList<>();

        NodeList fileNodes = projectTypeElement.getElementsByTagName(FILE);
        for (int j = 0; j < fileNodes.getLength(); j++) {
            Element fileElement = (Element) fileNodes.item(j);
            identifiers.add(fileElement.getTextContent());
        }

        NodeList folderNodes = projectTypeElement.getElementsByTagName(FOLDER);
        for (int j = 0; j < folderNodes.getLength(); j++) {
            Element folderElement = (Element) folderNodes.item(j);
            identifiers.add(folderElement.getTextContent());
        }
        return identifiers;
    }
}
