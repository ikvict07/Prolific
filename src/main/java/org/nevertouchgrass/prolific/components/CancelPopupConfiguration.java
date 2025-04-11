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
public class CancelPopupConfiguration {
    private final ApplicationContext applicationContext;

    @Bean
    @Lazy
    public ContextMenu cancellingPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = (Parent) applicationContext.getBean("cancellingDropdownParent");
        return getContextMenu(contextMenu, options);
    }

    @Bean
    @Lazy
    public ContextMenu cancelPopup() {
        ContextMenu contextMenu = new ContextMenu();
        var options = (Parent) applicationContext.getBean("cancellingDropdownParent");
        return getContextMenu(contextMenu, options);
    }
}
