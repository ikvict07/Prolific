package org.nevertouchgrass.prolific.model;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "configsParent")
@AllArgsConstructor
@NoArgsConstructor
public class RunConfigFileModel {
    private String projectPath;
    private List<RunConfig> configs;
}
