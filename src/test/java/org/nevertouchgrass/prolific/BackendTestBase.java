package org.nevertouchgrass.prolific;

import org.nevertouchgrass.prolific.components.CancelPopupConfiguration;
import org.nevertouchgrass.prolific.components.SettingsPopupConfiguration;
import org.nevertouchgrass.prolific.configuration.FXMLBeanFactoryPostProcessor;
import org.nevertouchgrass.prolific.configuration.JavaFXConfiguration;
import org.nevertouchgrass.prolific.configuration.LocalizationProviderBeanFactoryPostProcessor;
import org.nevertouchgrass.prolific.configuration.StageComponentAnnotationBeanPostProcessor;
import org.nevertouchgrass.prolific.javafxcontroller.FooterController;
import org.nevertouchgrass.prolific.javafxcontroller.HeaderController;
import org.nevertouchgrass.prolific.javafxcontroller.LogsAndMetricsPanelController;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@SuppressWarnings("unused")
public abstract class BackendTestBase {
    @MockitoBean
    private JavaFXConfiguration javaFXConfiguration;

    @MockitoBean
    private JavaFXApplication javaFXApplication;

    @MockitoBean
    private StageComponentAnnotationBeanPostProcessor stageComponentAnnotationBeanPostProcessor;
    @MockitoBean
    private FXMLBeanFactoryPostProcessor fxmlBeanFactoryPostProcessor;
    @MockitoBean
    private HeaderController headerController;
    @MockitoBean
    private SettingsPopupConfiguration settingsPopupConfiguration;
    @MockitoBean
    private CancelPopupConfiguration cancelPopupConfiguration;
    @MockitoBean
    private FooterController footerController;
    @MockitoBean
    private LogsAndMetricsPanelController logsAndMetricsPanelController;
    @MockitoBean
    private LocalizationProviderBeanFactoryPostProcessor localizationProviderBeanFactoryPostProcessor;
}
