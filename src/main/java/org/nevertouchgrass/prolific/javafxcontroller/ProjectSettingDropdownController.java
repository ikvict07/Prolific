package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;
import lombok.Data;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Data
@Scope("prototype")
public class ProjectSettingDropdownController {

    @FXML
    public Button starButton;
    @FXML
    public AnchorPane root;
    private Project project;

    private ProjectsRepository projectsRepository;


    @Autowired
    public void setProjectsRepository(ProjectsRepository projectsRepository) {
        this.projectsRepository = projectsRepository;
    }

    public void starProject() {
        project.setIsStarred(!project.getIsStarred());
        projectsRepository.update(project);
        var projectSettingsPopup = (Popup) root.getScene().getWindow();
        if (projectSettingsPopup != null) {
            projectSettingsPopup.hide();
        }

    }

    public void setProject(Project project) {
        this.project = project;
        starButton.setText((Boolean.TRUE.equals(project.getIsStarred()) ? "Unstar" : "Star") + " ☆");
    }
}
