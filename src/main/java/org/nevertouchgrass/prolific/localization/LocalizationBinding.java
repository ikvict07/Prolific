package org.nevertouchgrass.prolific.localization;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Component;

@Component
public class LocalizationBinding {

    private final LocalizationManager localizationManager;

    private final StringProperty settings = new SimpleStringProperty();
    private final StringProperty scan = new SimpleStringProperty();
    private final StringProperty plugins = new SimpleStringProperty();

    public LocalizationBinding(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
        updateLanguage();
    }

    public StringProperty settingsProperty() { return settings; }
    public StringProperty scanProperty() { return scan; }
    public StringProperty pluginsProperty() { return plugins; }

    public void updateLanguage() {
        settings.set(localizationManager.get("settings"));
        scan.set(localizationManager.get("scanner"));
        plugins.set(localizationManager.get("plugins"));
    }
}
