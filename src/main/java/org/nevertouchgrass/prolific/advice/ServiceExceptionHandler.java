package org.nevertouchgrass.prolific.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class ServiceExceptionHandler {
    @Lazy
    private final NotificationService notificationService;

    private final LocalizationProvider localizationProvider;

    @Around("execution(* org.nevertouchgrass.prolific.service.*.*(..)) && !execution(* org.nevertouchgrass.prolific.service.settings.UserSettingsService(..))")
    public Object handleException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            String methodName = joinPoint.getSignature().toShortString();
            log.error("Error in method {}: {}", methodName, e.getMessage(), e);

            notificationService.notifyError(ErrorNotification.of(e, localizationProvider.log_error_calling_method(), methodName));
            throw e;
        }
    }
}
