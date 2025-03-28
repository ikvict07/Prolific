package org.nevertouchgrass.prolific.components;


import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.nevertouchgrass.prolific.model.LogWrapper;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Queue;

@RequiredArgsConstructor
public class LogsAndMetricsTextComponent {
    private final ProcessLogs processLogs;
    private final Flux<LogWrapper> logsFlux;
    @Getter
    private final StyleClassedTextArea logsTextArea = new StyleClassedTextArea();
    @Getter
    private final VirtualizedScrollPane<StyleClassedTextArea> logsScrollPane = new VirtualizedScrollPane<>(logsTextArea);

    private volatile boolean followCaret = true;

    public void init() {
        logsTextArea.getStyleClass().add("text-flow");
        logsScrollPane.getStyleClass().add("scroll-pane");
        logsTextArea.setEditable(false);
        logsScrollPane.setMaxWidth(Double.MAX_VALUE);
        logsScrollPane.setMaxHeight(Double.MAX_VALUE);

        logsScrollPane.estimatedScrollYProperty().addListener((_, oldValue, newValue) -> setFollowCaret(newValue >= oldValue));

        HBox.setHgrow(logsScrollPane, Priority.ALWAYS);
        VBox.setVgrow(logsScrollPane, Priority.ALWAYS);

        Queue<LogWrapper> logs = processLogs.getLogs();
        logs.forEach(this::processLog);

        logsFlux.bufferTimeout(50, Duration.ofMillis(500))
                .subscribe(batch -> Platform.runLater(() -> {
                    batch.stream().sorted().forEach(this::processLog);

                    if (getFollowCaret()) {
                        logsTextArea.moveTo(logsTextArea.getLength());
                        logsTextArea.requestFollowCaret();
                    }
                }));
    }

    private void processLog(LogWrapper log) {
        String styleClass = switch(log.getLogType()) {
            case INFO -> "log-text";
            case ERROR -> "log-error-text";
        };

        int startPos = logsTextArea.getLength();
        logsTextArea.appendText(log.getLog() + "\n");
        logsTextArea.setStyleClass(startPos, logsTextArea.getLength(), styleClass);
    }

    private synchronized void setFollowCaret(boolean followCaret) {
        this.followCaret = followCaret;
    }

    private synchronized boolean getFollowCaret() {
        return followCaret;
    }
}
