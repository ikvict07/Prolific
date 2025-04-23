package org.nevertouchgrass.prolific.service.icons;

import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AnacondaProjectIconFactory extends AbstractIconFactory {
    @Setter(onMethod_ = @Autowired)
    private FxmlProvider fxmlProvider;

    public AnacondaProjectIconFactory() {
        super("anaconda");
    }

    @Override
    public StackPane configure() {
        return (StackPane) fxmlProvider.getIcon("anaconda");
    }
}
