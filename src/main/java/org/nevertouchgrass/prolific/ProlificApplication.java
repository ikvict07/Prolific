package org.nevertouchgrass.prolific;

import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.listener.JavaFxRuntimeInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * Basic spring boot class
 */
@SpringBootApplication
@EnableAsync
public class ProlificApplication {
    @SneakyThrows
    public static void main(String[] args) {
        var appBuilder = new SpringApplicationBuilder(ProlificApplication.class);
        appBuilder.headless(false);
        var app = appBuilder.build();
        app.addListeners(new JavaFxRuntimeInitializer());
        app.run(args);
    }
}

