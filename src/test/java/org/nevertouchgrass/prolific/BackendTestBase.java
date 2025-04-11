package org.nevertouchgrass.prolific;

import org.nevertouchgrass.prolific.configuration.JavaFXConfiguration;
import org.nevertouchgrass.prolific.javafxcontroller.FooterController;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SuppressWarnings("unused")
public abstract class BackendTestBase {
    @MockitoBean
    private JavaFXConfiguration javaFXConfiguration;
    @MockitoBean
    private JavaFXApplication javaFXApplication;
    @MockitoBean
    private FooterController footerController;
}
