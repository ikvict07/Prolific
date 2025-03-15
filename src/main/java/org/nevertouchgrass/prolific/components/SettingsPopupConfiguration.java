package org.nevertouchgrass.prolific.components;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.util.Pair;
import org.nevertouchgrass.prolific.javafxcontroller.ProjectSettingDropdownController;
import org.nevertouchgrass.prolific.model.FxmlLoadedResource;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SettingsPopupConfiguration {

    private FxmlProvider fxmlProvider;

    @Autowired
    public void set(FxmlProvider fxmlProvider) {
        this.fxmlProvider = fxmlProvider;
    }

    @Bean
    public ContextMenu settingsPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = fxmlProvider.getFxmlResource("settingsDropdown");
        return getContextMenu(contextMenu, options);
    }

    @Bean
    public Pair<ProjectSettingDropdownController, ContextMenu> projectSettingsPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = fxmlProvider.getFxmlResource("projectSettingDropdown");
        return new Pair<>((ProjectSettingDropdownController)options.getController(), getContextMenu(contextMenu, options));
    }

    private ContextMenu getContextMenu(ContextMenu contextMenu, FxmlLoadedResource<Object> options) {
        for (Node node : options.getParent().getChildrenUnmodifiable()) {
            Label label = (Label) node;
            MenuItem menuItem = new MenuItem(label.getText());
            menuItem.setGraphic(label.getGraphic());
            menuItem.setOnAction(_ -> node.getOnMouseClicked().handle(null));
            contextMenu.getItems().addAll(menuItem);
        }
        return contextMenu;
    }
}
