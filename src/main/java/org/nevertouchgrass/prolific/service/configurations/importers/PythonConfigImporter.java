package org.nevertouchgrass.prolific.service.configurations.importers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.model.RunConfig;
import org.nevertouchgrass.prolific.service.parser.DocumentParser;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.nevertouchgrass.prolific.service.configurations.importers.contract.ConfigImporter;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static org.nevertouchgrass.prolific.constants.XmlConfigConstants.CONFIGURATION;

@RequiredArgsConstructor
@Log4j2
@Service
public class PythonConfigImporter implements ConfigImporter {
    private final PathService pathService;
    private final DocumentParser documentParser;

    @Override
    public List<RunConfig> importConfig(Project project) {
        var workspacePaths = pathService.getWorkspacePaths(project);
        var configs = new ArrayList<RunConfig>();
        workspacePaths.forEach(workspacePath -> {
            if (!workspacePath.toFile().exists()) {
                return;
            }
            try {
                Document document = documentParser.parseXmlDocument(workspacePath);
                NodeList configurations = document.getElementsByTagName(CONFIGURATION);

                for (int i = 0; i < configurations.getLength(); i++) {
                    Element config = (Element) configurations.item(i);
                    String type = config.getAttribute("type");

                    if ("PythonConfigurationType".equals(type)) {
                        RunConfig runConfig = extractPythonConfig(config);
                        if (runConfig != null) {
                            configs.add(runConfig);
                        }
                    } else if ("Python.FastAPI".equals(type)) {
                        RunConfig runConfig = extractFastApiConfig(config);
                        if (runConfig != null) {
                            configs.add(runConfig);
                        }
                    }
                }

            } catch (Exception e) {
                log.error("Failed to import run configurations for project {}", project.getTitle(), e);
            }
        });
        return configs;
    }

    @Override
    public String getType() {
        return "Python";
    }

    private boolean isPython3Available() {
        try {
            Process process;
            ProcessBuilder builder;
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                builder = new ProcessBuilder("where", "python3");
            } else {
                builder = new ProcessBuilder("which", "python3");
            }
            process = builder.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            log.warn("Failed to check if python3 is available", e);
            return false;
        }
    }

    private RunConfig extractPythonConfig(Element config) {
        try {
            RunConfig runConfig = new RunConfig();
            runConfig.setConfigName(config.getAttribute("name"));
            runConfig.setType("Python");
            runConfig.setCommand(new ArrayList<>());

            NodeList options = config.getElementsByTagName("option");
            String scriptPath = null;
            String parameters = "";

            for (int i = 0; i < options.getLength(); i++) {
                Element option = (Element) options.item(i);
                String name = option.getAttribute("name");

                if ("SCRIPT_NAME".equals(name)) {
                    scriptPath = option.getAttribute("value");
                } else if ("PARAMETERS".equals(name)) {
                    parameters = option.getAttribute("value");
                }
            }

            if (scriptPath != null) {
                scriptPath = scriptPath.replace("$PROJECT_DIR$/", "");
                String pythonCommand = isPython3Available() ? "python3" : "python";
                runConfig.getCommand().add(pythonCommand);
                runConfig.getCommand().add(scriptPath);

                if (!parameters.isEmpty()) {
                    for (String param : parameters.split(" ")) {
                        if (!param.isEmpty()) {
                            runConfig.getCommand().add(param);
                        }
                    }
                }
                return runConfig;
            }
        } catch (Exception e) {
            log.error("Failed to extract Python configuration", e);
        }
        return null;
    }

    private RunConfig extractFastApiConfig(Element config) {
        try {
            RunConfig runConfig = new RunConfig();
            runConfig.setConfigName(config.getAttribute("name"));
            runConfig.setType("Python");
            runConfig.setCommand(new ArrayList<>());

            NodeList options = config.getElementsByTagName("option");
            String filePath = null;

            for (int i = 0; i < options.getLength(); i++) {
                Element option = (Element) options.item(i);
                String name = option.getAttribute("name");

                if ("file".equals(name)) {
                    filePath = option.getAttribute("value");
                }
            }

            if (filePath != null) {
                runConfig.getCommand().add("uvicorn");
                String moduleName = filePath.substring(filePath.lastIndexOf('/') + 1)
                        .replace(".py", "");
                runConfig.getCommand().add(moduleName + ":app");
                runConfig.getCommand().add("--reload");
                return runConfig;
            }
        } catch (Exception e) {
            log.error("Failed to extract FastAPI configuration", e);
        }
        return null;
    }
}