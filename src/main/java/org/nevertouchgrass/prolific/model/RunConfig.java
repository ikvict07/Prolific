package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RunConfig {
    private List<String> command = new ArrayList<>();
    private String configName;
    private String type;

    public List<String> getCommand() {
        command = new ArrayList<>(command.stream().filter(s -> !s.isEmpty()).toList());
        return command;
    }
}
