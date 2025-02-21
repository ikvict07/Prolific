package org.nevertouchgrass.prolific.events;

import org.springframework.context.ApplicationEvent;

public class StageShowEvent extends ApplicationEvent {
	public StageShowEvent(String source) {
		super(source);
	}

	public String getStage() {
		return (String) source;
	}
}
