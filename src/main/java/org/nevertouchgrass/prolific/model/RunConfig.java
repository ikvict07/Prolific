package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.util.List;

@Data
public class RunConfig {
    private List<String> command;
    private String configName;
    private String type;
}
