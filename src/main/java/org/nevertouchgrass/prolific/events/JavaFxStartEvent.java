package org.nevertouchgrass.prolific.events;

import org.springframework.context.ApplicationEvent;

/**
 * Event that will be created once when javafx runtime is initialized
 */
public class JavaFxStartEvent extends ApplicationEvent {
	public JavaFxStartEvent(Object source) {
		super(source);
	}
}
