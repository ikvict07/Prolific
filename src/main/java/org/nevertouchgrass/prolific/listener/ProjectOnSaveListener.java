package org.nevertouchgrass.prolific.listener;

import org.nevertouchgrass.prolific.annotation.OnSave;
import org.nevertouchgrass.prolific.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectOnSaveListener {

    @OnSave(Project.class)
    public void onSave(Project project) {
        System.out.println("I LISTENED" + project);
    }
}
