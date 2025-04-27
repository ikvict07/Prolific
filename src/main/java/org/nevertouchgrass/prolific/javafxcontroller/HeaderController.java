package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.service.ProjectsService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;

import java.nio.file.LinkOption;
import java.nio.file.Path;

@Lazy
@StageComponent
@Log4j2
@SuppressWarnings("unused")
public class HeaderController extends AbstractHeaderController {
    @FXML
    public StackPane settingsButton;
    @FXML
    public Circle minimizeButton;
    @FXML
    public Circle maximizeButton;
    @FXML
    public HBox gradientBox;
    @FXML
    public Label titleText;
    @FXML
    private AnchorPane header;
    @FXML
    private Circle closeButton;

    @Autowired
    public void setStage(@Qualifier("primaryStage") Stage stage) {
        this.stage = stage;
    }

    @Setter(onMethod_ = @Autowired)
    private ContextMenu settingsPopup;

    @Setter(onMethod_ = @Autowired)
    private ApplicationContext applicationContext;

    @Setter(onMethod_ = @Autowired)
    private ProjectsService projectsService;

    @Setter(onMethod_ = @Autowired)
    private ObjectFactory<Alert> alertFactory;

    @Initialize
    public void init() {
        setHeader(header);
        setupDragging();
        setupResizing();

        double minWidth = visualBounds.getMaxX() / 1.5;
        double minHeight = visualBounds.getMaxY() / 1.5;
        setMinWidth(minWidth);
        setMinHeight(minHeight);

        draggablePanes.add(header);
        draggablePanes.add(gradientBox);
        draggablePanes.add(titleText);
    }

    @Override
    public void handleClose() {
        log.info("Closing application");
        super.handleClose();
        SpringApplication.exit(applicationContext);
        log.info("Application closed");
    }

    public void dropdownForSettings() {
        Bounds bounds = settingsButton.localToScreen(settingsButton.getBoundsInLocal());
        settingsPopup.setX(bounds.getMinX());
        settingsPopup.setY(bounds.getMaxY());
        settingsPopup.show(stage);
    }

    public void projects() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Open Project");
        try {
            String f = fileChooser.showDialog(stage).getPath();
            Path p = Path.of(f).toRealPath(LinkOption.NOFOLLOW_LINKS);
            projectsService.manuallyAddProject(p);
        } catch (NullPointerException ignore) {} catch (Exception e) {
            log.error("Exception trying to open the project: {}", e.getMessage());
            showAlert();
        }
    }

    private void showAlert() {
        var alert = alertFactory.getObject();
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("Unknown project type");
        alert.showAndWait();
    }
}
