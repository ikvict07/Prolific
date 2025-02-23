package org.nevertouchgrass.prolific.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
