package org.nevertouchgrass.prolific.listener;

import javafx.application.Platform;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;

public class JavaFxRuntimeInitializer implements ApplicationListener<ApplicationStartingEvent> {
	@Override
	public void onApplicationEvent(@NonNull ApplicationStartingEvent event) {
		System.out.println("Initializing JavaFX runtime");

		Platform.startup(() -> System.out.println("JavaFX runtime initialized."));
	}
}
