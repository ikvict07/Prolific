package org.nevertouchgrass.prolific.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private String title;
    private String type;
    private String path;
    private Boolean isManuallyAdded = false;
}
