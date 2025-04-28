package org.nevertouchgrass.prolific.listener;


import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.StageInitializeEvent;
import org.nevertouchgrass.prolific.service.localization.LocalizationHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Calls all method marked by @Initialize
 *
 * @see Initialize
 */
@Slf4j
@SuppressWarnings("NullableProblems")
@Component
@RequiredArgsConstructor
public class InitializeAnnotationProcessor implements ApplicationListener<StageInitializeEvent> {
    private final ApplicationContext applicationContext;
    private final LocalizationHolder localizationHolder;

    @Override
    public void onApplicationEvent(StageInitializeEvent event) {
        applicationContext.getBeansWithAnnotation(StageComponent.class).forEach((_, bean) -> {
            StageComponent annotation = bean.getClass().getAnnotation(StageComponent.class);
            if (annotation != null && !annotation.stage().equals(event.getStage())) {
                return;
            }
            var methods = bean.getClass().getDeclaredMethods();
            Arrays.stream(methods).filter(method -> method.isAnnotationPresent(Initialize.class)).forEach(method -> {
                method.setAccessible(true);
                try {
                    method.invoke(bean);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            Arrays.stream(bean.getClass().getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);
                try {
                    if (Node.class.isAssignableFrom(field.getType())) {
                        var object = field.get(bean);
                        if (object == null) {
                            return;
                        }
                        var node = (Node) object;
                        var userData = node.getUserData();
                        if (userData == null) {
                            return;
                        }
                        var userDataString = userData.toString();
                        var localizationPart = Arrays.stream(userDataString.split(",")).filter(d ->
                                d.startsWith("localization:")).findFirst();
                        if (localizationPart.isEmpty()) {
                            return;
                        }
                        var localizationKey = localizationPart.get().replace("localization:", "");
                        var property = (StringProperty) Arrays.stream(object.getClass().getMethods()).filter(m ->
                                m.getName().equals("textProperty")).findFirst().get().invoke(node);
                        property.bind(localizationHolder.getLocalization(localizationKey));
                    }
                } catch (Exception e) {
                    // IDC
                    log.error("Error while processing field: {}", field.getName(), e);
                }
            });
        });
    }
}
