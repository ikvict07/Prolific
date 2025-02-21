package org.nevertouchgrass.prolific;

import org.nevertouchgrass.prolific.listener.PreBeanCreationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProlificApplication {
	public static void main(String[] args) {
		var app = new SpringApplication(ProlificApplication.class);
		app.addListeners(new PreBeanCreationListener());
		app.run(args);
	}
}
