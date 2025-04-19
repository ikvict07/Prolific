package org.nevertouchgrass.prolific.components;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.InlineCssTextArea;
import org.nevertouchgrass.prolific.model.LogWrapper;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class LogsAndMetricsTextComponent {
    private static final int MAX_LOG_ENTRIES = 5000;
    private static final int BATCH_SIZE = 100;
    private static final int DELETION_THRESHOLD = 1000;
    private static final String ERROR_CLASS = "-fx-fill: #DB5C5C; -fx-font-family: \"JetBrains Mono\"; -fx-background-color: transparent;";

    private final ProcessLogs processLogs;
    private final Flux<LogWrapper> logsFlux;

    @Getter
    private final InlineCssTextArea textArea = new InlineCssTextArea();
    private final VirtualizedScrollPane<InlineCssTextArea> scrollPane = new VirtualizedScrollPane<>(textArea);

    private volatile boolean followCaret = true;
    private int currentLineCount = 0;

    public void init() {
        configureTextArea();
        setupScrollListener();
        scrollPane.getStyleClass().add("scroll-pane");
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        processBatchesWithDelay(new ArrayList<>(processLogs.getLogs()), 0);

        logsFlux.bufferTimeout(100, Duration.ofMillis(250))
                .subscribeWith(new BaseSubscriber<List<LogWrapper>>() {
                    @Override
                    protected void hookOnSubscribe(Subscription subscription) {
                        request(1);
                    }

                    @Override
                    protected void hookOnNext(List<LogWrapper> batch) {
                        if (batch.isEmpty()) {
                            request(1);
                            return;
                        }

                        List<LogWrapper> sortedBatch = new ArrayList<>(batch);
                        Collections.sort(sortedBatch);

                        Platform.runLater(() -> {
                            processLogBatch(sortedBatch);
                            request(1);
                        });
                    }
                });
    }

    private void configureTextArea() {
        textArea.setEditable(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.getStyleClass().add("text-flow");
        textArea.setUseInitialStyleForInsertion(true);
    }

    private void setupScrollListener() {
        textArea.estimatedScrollYProperty().addListener((_, oldVal, newVal) -> {
            double maxValue = textArea.getTotalHeightEstimate();
            double viewportHeight = textArea.getViewportHeight();
            double currentPos = newVal + viewportHeight;

            boolean isAtBottom = currentPos >= maxValue - viewportHeight * 0.1;
            boolean isScrollingDown = newVal > oldVal;
            setFollowCaret(isAtBottom || (isScrollingDown && getFollowCaret()));
        });

        scrollPane.setOnScroll(event -> {
            if (event.getDeltaY() < 0) {
                setFollowCaret(true);
            } else if (event.getDeltaY() > 0) {
                setFollowCaret(false);
            }
        });
    }

    private void processBatchesWithDelay(List<LogWrapper> logs, int startIndex) {
        if (startIndex >= logs.size()) return;

        int endIndex = Math.min(startIndex + BATCH_SIZE, logs.size());
        List<LogWrapper> batch = logs.subList(startIndex, endIndex);

        Platform.runLater(() -> {
            processLogBatch(batch);

            if (endIndex < logs.size()) {
                Platform.runLater(() -> processBatchesWithDelay(logs, endIndex));
            } else if (getFollowCaret()) {
                scrollToEnd();
            }
        });
    }

    private void processLogBatch(List<LogWrapper> logs) {
        if (logs.isEmpty()) return;

        StringBuilder batchText = new StringBuilder();
        List<Object> batchStyleInfo = new ArrayList<>();

        for (LogWrapper log : logs) {
            String logText = log.getLog() + System.lineSeparator();
            batchText.append(logText);

            batchStyleInfo.add(log.getLogType());
            batchStyleInfo.add(logText.length());
        }

        int startPos = textArea.getLength();
        textArea.appendText(batchText.toString());

        int stylePos = startPos;
        for (int i = 0; i < batchStyleInfo.size(); i += 2) {
            Object logType = batchStyleInfo.get(i);
            int length = (Integer) batchStyleInfo.get(i + 1);

            if (logType.toString().equals("ERROR")) {
                textArea.setStyle(stylePos, stylePos + length, ERROR_CLASS);
            }

            stylePos += length;
        }

        int addedLines = batchText.toString().split("\n|\r\n").length;
        currentLineCount += addedLines;

        if (currentLineCount > MAX_LOG_ENTRIES + DELETION_THRESHOLD) {
            int linesToRemove = DELETION_THRESHOLD;
            int endOfLinesToRemove = getPositionOfLine(linesToRemove);

            textArea.deleteText(0, endOfLinesToRemove);
            currentLineCount -= linesToRemove;
        }

        // Auto-scroll
        if (getFollowCaret()) {
            scrollToEnd();
        }
    }


    private int getPositionOfLine(int lineNumber) {
        return textArea.position(lineNumber, 0).toOffset();
    }

    private void scrollToEnd() {
        textArea.moveTo(textArea.getLength());
        textArea.requestFollowCaret();
    }

    private synchronized void setFollowCaret(boolean followCaret) {
        this.followCaret = followCaret;
    }

    private synchronized boolean getFollowCaret() {
        return followCaret;
    }

    public Node getComponent() {
        return scrollPane;
    }
}