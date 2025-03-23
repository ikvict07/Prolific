package org.nevertouchgrass.prolific.advice;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.nevertouchgrass.prolific.model.notification.ErrorNotification;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Log4j2
public class ServiceExceptionHandler {
    private final NotificationService notificationService;

    @Around("execution(* org.nevertouchgrass.prolific.service.*.*(..))")
    public Object handleException(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            // Логируем ошибку
            String methodName = joinPoint.getSignature().toShortString();
            log.error("Error in method {}: {}", methodName, e.getMessage(), e);

            notificationService.notifyError(ErrorNotification.of(e, "Error occurred while calling method: {}", methodName));
            throw e;
        }
    }
}
