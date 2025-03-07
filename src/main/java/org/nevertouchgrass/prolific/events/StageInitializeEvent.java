package org.nevertouchgrass.prolific.events;

import org.springframework.context.ApplicationEvent;

public class StageInitializeEvent extends ApplicationEvent {
	public StageInitializeEvent(String source) {
		super(source);
	}

	public String getStage() {
		return (String) source;
	}
}
