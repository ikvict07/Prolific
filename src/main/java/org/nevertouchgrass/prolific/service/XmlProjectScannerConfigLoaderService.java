package org.nevertouchgrass.prolific.service;

import lombok.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.nevertouchgrass.prolific.constants.XmlConfigConstants.*;
import org.nevertouchgrass.prolific.model.ProjectTypeModel;
import org.springframework.stereotype.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class XmlProjectScannerConfigLoaderService {

	private static final Logger LOGGER = LogManager.getLogger();

	public List<ProjectTypeModel> loadProjectTypes(@NonNull String path) {
		List<ProjectTypeModel> projectTypeModels = new ArrayList<>();

		try {
			File file = new File(path);
			if (!file.exists()) {
				throw new RuntimeException("Plugin configuration file does not exist " + path);
			}

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
