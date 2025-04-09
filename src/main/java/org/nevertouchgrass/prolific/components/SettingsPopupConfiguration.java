package org.nevertouchgrass.prolific.components;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.util.Pair;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.javafxcontroller.ProjectSettingDropdownController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class SettingsPopupConfiguration {
    private final ApplicationContext applicationContext;

    @Bean
    public ContextMenu settingsPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = (Parent)applicationContext.getBean("settingsDropdownParent");
        return getContextMenu(contextMenu, options);
    }

    @Bean
    public Pair<ProjectSettingDropdownController, ContextMenu> projectSettingsPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = (Parent) applicationContext.getBean("projectSettingDropdownParent");
        return new Pair<>((ProjectSettingDropdownController)options.getProperties().get("controller"), getContextMenu(contextMenu, options));
    }

    private ContextMenu getContextMenu(ContextMenu contextMenu, Parent options) {
        for (Node node : options.getChildrenUnmodifiable()) {
            Label label = (Label) node;
            MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(label.textProperty());
            menuItem.setGraphic(label.getGraphic());
            menuItem.setOnAction(_ -> node.getOnMouseClicked().handle(null));
            contextMenu.getItems().addAll(menuItem);
        }
        return contextMenu;
    }
}
