package org.nevertouchgrass.prolific.model;

import lombok.Getter;

import java.util.List;

@Getter
public class ProjectTypeModel {
    private final String name;
    private final List<String> identifiers;

    public ProjectTypeModel(String name, List<String> identifiers) {
        this.name = name;
        this.identifiers = identifiers;
    }
}
