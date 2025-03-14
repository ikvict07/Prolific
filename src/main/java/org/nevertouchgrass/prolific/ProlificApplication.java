package org.nevertouchgrass.prolific;

import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.listener.JavaFxRuntimeInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Basic spring boot class
 */
@SpringBootApplication
@EnableAsync
public class ProlificApplication {
    @SneakyThrows
    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        String dbPath = userHome + "/Prolific/data/prolific.sqlite";
        Files.createDirectories(Path.of(dbPath).getParent());
        System.setProperty("spring.datasource.url", "jdbc:sqlite:" + dbPath);
        var appBuilder = new SpringApplicationBuilder(ProlificApplication.class);
        appBuilder.headless(false);
        var app = appBuilder.build();
        app.addListeners(new JavaFxRuntimeInitializer());
        app.run(args);
    }
}

