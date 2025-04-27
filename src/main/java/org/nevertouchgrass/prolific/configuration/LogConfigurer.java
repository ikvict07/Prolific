package org.nevertouchgrass.prolific.configuration;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class LogConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(LogConfigurer.class);
    private final PathService pathService;

    @PostConstruct
    public void configureLogFile() {
        String logFilePath = getUserDefinedLogPath();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("FILE");
        fileAppender.setFile(logFilePath);
        fileAppender.setAppend(true);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        ch.qos.logback.classic.Logger rootLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        rootLogger.addAppender(fileAppender);

        System.setProperty("LOG_FILE", logFilePath);

        logger.info("Logging configured to file: {}", logFilePath);
    }

    private String getUserDefinedLogPath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String timestamp = LocalDateTime.now().format(formatter);
        return pathService.getProjectFilesPath().resolve("logs").resolve("prolific-" + timestamp + ".log").toString();
    }
}