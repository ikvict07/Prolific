package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.*;

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
import org.nevertouchgrass.prolific.constants.profile.CommonUser;
import org.nevertouchgrass.prolific.constants.profile.NoMetricsUser;
import org.nevertouchgrass.prolific.constants.profile.PowerUser;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.ProjectsService;
import org.nevertouchgrass.prolific.service.localization.LocalizationHolder;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
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
    public HBox profilesPanel;
    @FXML
    ComboBox<ProfileItem> userList = new ComboBox<>();
    @FXML
    private AnchorPane header;
    @FXML
    private Circle closeButton;
    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    @Setter(onMethod_ = @Autowired)
    private LocalizationHolder localizationHolder;
    @Setter(onMethod_ = @Autowired)
    private UserSettingsService userSettingsService;
    @Setter(onMethod_ = @Autowired)
    private UserSettingsHolder userSettingsHolder;

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
        header.requestFocus();
        userList.getStyleClass().clear();
        userList.getStyleClass().add("profiles-combo-box");
        userList.getStyleClass().add("combo-box-base");
        var powerUser = new ProfileItem(localizationHolder.getLocalization(PowerUser.PROFILE));
        var commonUser = new ProfileItem(localizationHolder.getLocalization(CommonUser.PROFILE));
        var noMetricsUser = new ProfileItem(localizationHolder.getLocalization(NoMetricsUser.PROFILE));

        userList.getItems().addAll(powerUser, commonUser, noMetricsUser);

        userList.setCellFactory(lv -> createProfileItemCell());
        userList.setButtonCell(createProfileItemCell());
        userList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                if (newValue.equals(powerUser)) {
                    userSettingsHolder.setUserRole(PowerUser.PROFILE);
                } else if (newValue.equals(commonUser)) {
                    userSettingsHolder.setUserRole(CommonUser.PROFILE);
                }  else if (newValue.equals(noMetricsUser)) {
                    userSettingsHolder.setUserRole(NoMetricsUser.PROFILE);
                }
                userSettingsService.saveSettings();
            }
        });
        var currentUser = userSettingsHolder.getUser();
        if (currentUser instanceof PowerUser) {
            userList.getSelectionModel().select(powerUser);
        } else if (currentUser instanceof CommonUser) {
            userList.getSelectionModel().select(commonUser);
        } else if (currentUser instanceof NoMetricsUser) {
            userList.getSelectionModel().select(noMetricsUser);
        }
    }

    private static ListCell<ProfileItem> createProfileItemCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(ProfileItem item, boolean empty) {
                textProperty().unbind();

                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    textProperty().bind(item.displayTextProperty());
                }
            }
        };
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


    public static class ProfileItem {
        private final StringProperty displayText;

        public ProfileItem(StringProperty displayText) {
            this.displayText = displayText;
        }

        public StringProperty displayTextProperty() {
            return displayText;
        }

        @Override
        public String toString() {
            return displayText.get();
        }
    }


}
