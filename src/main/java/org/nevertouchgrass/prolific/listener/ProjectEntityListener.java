package org.nevertouchgrass.prolific.listener;

import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.OnDelete;
import org.nevertouchgrass.prolific.annotation.OnSave;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@SuppressWarnings("unused")
public class ProjectEntityListener {

    @OnSave(Project.class)
    public void onSave(Project project) {
        log.info("Project saved: {}", project);
    }

    @OnDelete(Project.class)
    public void onDelete(Project project) {
        log.info("Project deleted: {}", project);
    }
}
