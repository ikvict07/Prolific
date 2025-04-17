package org.nevertouchgrass.prolific.configuration;

import javafx.beans.property.StringProperty;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.service.localization.LocalizationHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;

@Component
@RequiredArgsConstructor
public class LocalizationProviderInvocationHandler implements InvocationHandler {
    private final LocalizationHolder localizationHolder;

    @Override
    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) {
        if (method.getReturnType().equals(StringProperty.class) && !method.getDeclaringClass().equals(Object.class)) {
            return localizationHolder.getLocalization(method.getName());
        }
        throw new IllegalStateException("Method " + method.getName() + " is an Object's method or does not return StringProperty");
    }
}
