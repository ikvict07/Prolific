package org.nevertouchgrass.prolific.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ContextMenuCreator {
    public static ContextMenu getContextMenu(ContextMenu contextMenu, Parent options) {
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
