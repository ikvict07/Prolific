package org.nevertouchgrass.prolific.javafxcontroller.settings.contract;

import javafx.scene.Node;

import java.util.List;

public interface SettingsOption {
    void setupValidators();
    boolean validInput();
    boolean checkDefaultValues();
    boolean saveSettings();
    List<Node> getOptions();
}
