package org.nevertouchgrass.prolific.components;

import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import static org.nevertouchgrass.prolific.util.ContextMenuCreator.getContextMenu;


@Configuration
@RequiredArgsConstructor
public class SettingsPopupConfiguration {
    private final ApplicationContext applicationContext;

    @Bean
    @Lazy
    public ContextMenu settingsPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = (Parent)applicationContext.getBean("settingsDropdownParent");
        return getContextMenu(contextMenu, options);
    }

    @Bean
    @Lazy
    public ContextMenu projectSettingsPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = (Parent) applicationContext.getBean("projectSettingDropdownParent");
        return getContextMenu(contextMenu, options);
    }

}
