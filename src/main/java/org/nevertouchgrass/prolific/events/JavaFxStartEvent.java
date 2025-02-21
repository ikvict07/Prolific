package org.nevertouchgrass.prolific.events;

import org.springframework.context.ApplicationEvent;

public class JavaFxStartEvent extends ApplicationEvent {
	public JavaFxStartEvent(Object source) {
		super(source);
	}
}
