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
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.getContent().add(settingsDropdownParent);
        return popup;
    }
}
