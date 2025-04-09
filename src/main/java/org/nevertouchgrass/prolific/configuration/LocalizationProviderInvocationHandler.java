package org.nevertouchgrass.prolific.configuration;

import javafx.beans.property.StringProperty;
import org.nevertouchgrass.prolific.service.localization.LocalizationHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;

@Component
public class LocalizationProviderInvocationHandler implements InvocationHandler {

    private LocalizationHolder localizationHolder;

    @Autowired
    public void setLocalizationHolder(LocalizationHolder localizationHolder) {
        this.localizationHolder = localizationHolder;
    }

    @Override
    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
        if (method.isDefault() && method.getReturnType().equals(StringProperty.class) && !method.getDeclaringClass().equals(Object.class)) {
            Class<?> declaringClass = method.getDeclaringClass();
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(declaringClass, lookup);

            MethodHandle methodHandle = privateLookup.findSpecial(declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass);
            var result =  methodHandle.bindTo(proxy).invokeWithArguments(args);
            return localizationHolder.getLocalization(((StringProperty) result).get());
        }
        throw new IllegalStateException("Method " + method.getName() + " is not a default method or does not return StringProperty");
    }
}
