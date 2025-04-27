package org.nevertouchgrass.prolific.javafxcontroller.settings.contract;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;

public interface SettingsListController {
    @FXML
    void handleMouseEntered();
    @FXML
    void switchOptions(SettingsOption option, Node label);
    @FXML
    void setSettingsList(Event event);
}
