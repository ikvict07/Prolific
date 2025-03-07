package org.nevertouchgrass.prolific.events;

import org.springframework.context.ApplicationEvent;


/**
 * Event that will be created when the stage is initialized
 *
 * @see org.nevertouchgrass.prolific.listener.InitializeAnnotationProcessor
 * @see org.nevertouchgrass.prolific.annotation.Initialize
 */
public class StageInitializeEvent extends ApplicationEvent {
    public StageInitializeEvent(String source) {
        super(source);
    }

    public String getStage() {
        return (String) source;
    }
}
