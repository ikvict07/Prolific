package org.nevertouchgrass.prolific;

import org.nevertouchgrass.prolific.listener.JavaFxRuntimeInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProlificApplication {
    public static void main(String[] args) {
        var app = new SpringApplication(ProlificApplication.class);
        app.addListeners(new JavaFxRuntimeInitializer());
        app.run(args);
    }
}

