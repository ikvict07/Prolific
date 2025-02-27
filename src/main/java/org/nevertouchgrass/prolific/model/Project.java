package org.nevertouchgrass.prolific.model;

import lombok.Data;

import java.nio.file.Path;

@Data
public class Project {
    private String title;
    private String type;
    private String path;
}
