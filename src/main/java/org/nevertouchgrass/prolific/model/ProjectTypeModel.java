package org.nevertouchgrass.prolific.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@EqualsAndHashCode
@ToString
public class ProjectTypeModel {
    private final String name;
    private final List<String> identifiers;

    public ProjectTypeModel(String name, List<String> identifiers) {
        this.name = name;
        this.identifiers = identifiers;
    }
}
