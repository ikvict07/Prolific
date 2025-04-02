package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.constants.action.ExcludeProjectAction;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.ProjectExcluderService;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.nevertouchgrass.prolific.service.permissions.contract.PermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Component
@Data
@Scope("prototype")
@Log4j2
public class ProjectSettingDropdownController {

    @FXML
    public Label starButton;
    @FXML
    public VBox root;
    @FXML
    public Label openInExplorerButton;
    @FXML
    public Label excludeProjectButton;
    private Project project;

    private ProjectsRepository projectsRepository;
    private PermissionRegistry permissionRegistry;
    private ProjectExcluderService projectExcluderService;

    @Autowired
    public void setPermissionRegistry(PermissionRegistry permissionRegistry) {
        this.permissionRegistry = permissionRegistry;
    }

    @Autowired
    public void setProjectExcluderService(ProjectExcluderService projectExcluderService) {
        this.projectExcluderService = projectExcluderService;
    }

    @Autowired
    public void setProjectsRepository(ProjectsRepository projectsRepository) {
        this.projectsRepository = projectsRepository;
    }

    public void starProject() {
        project.setIsStarred(!project.getIsStarred());
        projectsRepository.update(project);
    }

    public void openInExplorer() {
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    File file = Path.of(project.getPath()).toFile();
                    if (file.exists()) {
                        Desktop.getDesktop().open(file);
                    } else {
                        log.error("Path does not exist: {}", project.getPath());
                    }
                } else {
                    log.error("Desktop is not supported");
                }
            } catch (IOException e) {
                log.error("Error opening file in explorer: {}", e.getMessage());
            }
        }).start();
    }

    public void setProject(Project project, ContextMenu contextMenu) {
        this.project = project;

        contextMenu.getItems().clear();
        starButton.setText((Boolean.TRUE.equals(project.getIsStarred()) ? "Unstar" : "Star"));
        excludeProjectButton.setVisible(checkPermission());
        for (Node node : root.getChildren()) {
            MenuItem menuItem = new MenuItem(((Label) node).getText());
            if (((Label) node).getGraphic() != null) {
                menuItem.setGraphic(((Label) node).getGraphic());
            }
            menuItem.setOnAction(_ -> node.getOnMouseClicked().handle(null));
            contextMenu.getItems().add(menuItem);
        }
    }

    private boolean checkPermission() {
        var action = new ExcludeProjectAction(project);
        var checker = permissionRegistry.getChecker(action.getClass());
        if (checker != null) {
            @SuppressWarnings("unchecked")
            PermissionChecker<ExcludeProjectAction> castedChecker =
                    (PermissionChecker<ExcludeProjectAction>) checker;
            return castedChecker.hasPermission(action);
        } else {
            log.error("No permission checker found for action {}", action.getClass().getName());
            return false;
        }
    }

    public void excludeProject() {
        projectExcluderService.excludeProject(new ExcludeProjectAction(project));
    }
}
