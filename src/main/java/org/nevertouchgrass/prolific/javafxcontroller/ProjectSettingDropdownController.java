package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.constants.action.DeleteProjectAction;
import org.nevertouchgrass.prolific.constants.action.ExcludeProjectAction;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.repository.ProjectsRepository;
import org.nevertouchgrass.prolific.service.ProjectDeleteService;
import org.nevertouchgrass.prolific.service.ProjectExcluderService;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.permissions.PermissionRegistry;
import org.nevertouchgrass.prolific.service.permissions.contract.PermissionChecker;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@StageComponent
@Data
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
    @FXML
    public Label deleteProjectButton;
    private Project project;
    @Setter(onMethod_ = @Autowired)
    private ProjectsRepository projectsRepository;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    @Setter(onMethod_ = @Autowired)
    private PermissionRegistry permissionRegistry;
    @Setter(onMethod_ = @Autowired)
    private ProjectExcluderService projectExcluderService;
    @Setter(onMethod_ = @Autowired)
    private ProjectDeleteService projectDeleteService;


    @FXML
    public void initialize() {
        starButton.textProperty().bind(localizationProvider.star());
        openInExplorerButton.textProperty().bind(localizationProvider.directory());
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
        starButton.textProperty().bind(Boolean.TRUE.equals(project.getIsStarred()) ? localizationProvider.unstar() : localizationProvider.star());
        excludeProjectButton.setVisible(checkExcludePermission());
        deleteProjectButton.setVisible(checkDeletePermission());
        for (Node node : root.getChildren()) {
            if (!node.isVisible()) continue;
            MenuItem menuItem = new MenuItem(((Label) node).getText());
            if (((Label) node).getGraphic() != null) {
                menuItem.setGraphic(((Label) node).getGraphic());
            }
            menuItem.setOnAction(_ -> node.getOnMouseClicked().handle(null));
            contextMenu.getItems().add(menuItem);
        }
    }

    private boolean checkExcludePermission() {
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
    private boolean checkDeletePermission() {
        var action = new DeleteProjectAction(project);
        var checker = permissionRegistry.getChecker(action.getClass());
        if (checker != null) {
            @SuppressWarnings("unchecked")
            PermissionChecker<DeleteProjectAction> castedChecker =
                    (PermissionChecker<DeleteProjectAction>) checker;
            return castedChecker.hasPermission(action);
        } else {
            log.error("No permission checker found for action {}", action.getClass().getName());
            return false;
        }
    }

    public void excludeProject() {
        projectExcluderService.excludeProject(new ExcludeProjectAction(project));
    }

    @SneakyThrows
    public void deleteProject() {
        Alert confirmDialog = new Alert(Alert.AlertType.NONE);
        Pane graphic = new FXMLLoader(getClass().getResource("/icons/fxml/warning.fxml")).load();
        confirmDialog.setGraphic(graphic);

        confirmDialog.setTitle(localizationProvider.delete_confirm_title().get());
        confirmDialog.setHeaderText(localizationProvider.delete_confirm_header().get());

        ButtonType buttonTypeYes = new ButtonType(localizationProvider.settings_submit_button().get(), ButtonBar.ButtonData.YES);
        ButtonType buttonTypeNo = new ButtonType(localizationProvider.settings_cancel_button().get(), ButtonBar.ButtonData.NO);
        confirmDialog.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        DialogPane dialogPane = confirmDialog.getDialogPane();
        Text text = new Text(localizationProvider.delete_confirm_content().get());
        text.getStyleClass().clear();
        text.getStyleClass().add("dialog-pane-text");
        text.wrappingWidthProperty().bind(dialogPane.widthProperty().subtract(40));

        dialogPane.setContent(text);
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/styles.css")).toExternalForm());

        Button submitButton = (Button) dialogPane.lookupButton(buttonTypeYes);
        submitButton.getStyleClass().clear();
        submitButton.getStyleClass().add("settings-submit-button");
        submitButton.setAlignment(Pos.CENTER);

        Button cancelButton = (Button) dialogPane.lookupButton(buttonTypeNo);
        cancelButton.getStyleClass().clear();
        cancelButton.getStyleClass().add("settings-cancel-button");
        cancelButton.setAlignment(Pos.CENTER);

        confirmDialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == buttonTypeYes) {
                projectDeleteService.deleteProject(new DeleteProjectAction(project));
            }
        });
    }
}
