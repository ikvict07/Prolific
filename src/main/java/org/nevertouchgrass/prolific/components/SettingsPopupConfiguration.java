package org.nevertouchgrass.prolific.components;

import javafx.scene.Parent;
import javafx.stage.Popup;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SettingsPopupConfiguration {

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Popup settingsPopup(Parent settingsDropdownParent) {
        Popup popup = new Popup();
        popup.setAutoHide(false);
        popup.focusedProperty().addListener((_, _, newValue) -> {
            if (!newValue) {
                popup.hide();
            }
        });
        popup.setAutoFix(true);
        popup.getContent().add(settingsDropdownParent);
        return popup;
    }

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public Popup projectSettingsPopup(Parent projectSettingDropdownParent) {
        Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.getContent().add(projectSettingDropdownParent);
        popup.getProperties().put("content", projectSettingDropdownParent);
        return popup;
    }
}
