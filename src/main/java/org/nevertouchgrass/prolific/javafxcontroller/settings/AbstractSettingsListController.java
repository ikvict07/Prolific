package org.nevertouchgrass.prolific.javafxcontroller.settings;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.nevertouchgrass.prolific.components.ArrayListHolder;
import org.nevertouchgrass.prolific.javafxcontroller.settings.contract.SettingsListController;
import org.nevertouchgrass.prolific.javafxcontroller.settings.contract.SettingsOption;

public abstract class AbstractSettingsListController implements SettingsListController {
    @FXML protected ArrayListHolder<Node> settingsLabels;
    @FXML protected GridPane settingsList;
    protected SettingsOption currentSettingsOption;

    private static final String SELECTED = "selected";

    @FXML
    public void switchOptions(SettingsOption option, Node label) {
        if (currentSettingsOption == option) {
            return;
        }

        if (currentSettingsOption != null) {
            currentSettingsOption.resetToDefaults();
        }

        settingsLabels.getItems().forEach(l -> l.getStyleClass().remove(SELECTED));
        label.getStyleClass().add(SELECTED);
        currentSettingsOption = option;
        settingsList.getChildren().clear();
        settingsList.getChildren().addAll(option.getOptions());
        option.setupValidators();
    }
}
