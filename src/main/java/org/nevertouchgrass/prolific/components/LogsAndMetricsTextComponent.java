package org.nevertouchgrass.prolific.components;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.nevertouchgrass.prolific.model.LogWrapper;
import org.nevertouchgrass.prolific.model.ProcessLogs;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@RequiredArgsConstructor
public class LogsAndMetricsTextComponent {
    private static final int MAX_LOG_ENTRIES = 5000;
    private static final int BATCH_SIZE = 100;

    private final ProcessLogs processLogs;
    private final Flux<LogWrapper> logsFlux;

    @Getter
    private final ListView<LogWrapper> logListView = new ListView<>();
    private final ObservableList<LogWrapper> visibleLogs = FXCollections.observableArrayList();

    private final ConcurrentLinkedQueue<LogWrapper> logBuffer = new ConcurrentLinkedQueue<>();
    private volatile boolean followCaret = true;

    public void init() {
        logListView.setItems(visibleLogs);
        logListView.setCellFactory(createCellFactory());
        logListView.setMaxWidth(Double.MAX_VALUE);
        logListView.setMaxHeight(Double.MAX_VALUE);


        Platform.runLater(this::setupScrollListener);

        HBox.setHgrow(logListView, Priority.ALWAYS);
        VBox.setVgrow(logListView, Priority.ALWAYS);

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
                        logBuffer.addAll(sortedBatch);

                        Platform.runLater(() -> {
                            processLogBatch(sortedBatch);
                            request(1);
                        });
                    }
                });
    }

    private void setupScrollListener() {
        for (Node node : logListView.lookupAll(".scroll-bar")) {
            if (node instanceof ScrollBar scrollBar && scrollBar.getOrientation().equals(Orientation.VERTICAL)) {
                scrollBar.valueProperty().addListener((obs, oldVal, newVal) -> {
                    double maxValue = scrollBar.getMax();
                    boolean isAtBottom = newVal.doubleValue() >= maxValue - 0.02;
                    boolean isScrollingDown = newVal.doubleValue() > oldVal.doubleValue();
                    setFollowCaret(isAtBottom || (isScrollingDown && getFollowCaret()));
                });
                break;
            }

        }

        logListView.setOnScroll(event -> {
            if (event.getDeltaY() < 0) {
                // Scrolling down
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
            } else if (getFollowCaret() && !visibleLogs.isEmpty()) {
                logListView.scrollTo(visibleLogs.size() - 1);
            }
        });
    }

    private void processLogBatch(List<LogWrapper> logs) {
        if (logs.isEmpty()) return;

        visibleLogs.addAll(logs);

        // Trim if needed
        while (visibleLogs.size() > MAX_LOG_ENTRIES) {
            visibleLogs.remove(0);
        }

        // Auto-scroll
        if (getFollowCaret() && !visibleLogs.isEmpty()) {
            logListView.scrollTo(visibleLogs.size() - 1);
        }
    }

    private Callback<ListView<LogWrapper>, ListCell<LogWrapper>> createCellFactory() {
        return listView -> new ListCell<>() {
            private final Label label = new Label();

            {
                label.setWrapText(true);
                label.setMouseTransparent(false);
            }

            @Override
            protected void updateItem(LogWrapper log, boolean empty) {
                super.updateItem(log, empty);
                if (empty || log == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    label.setText(log.getLog());
                    label.getStyleClass().removeAll("log-text", "log-error-text");

                    boolean isInfo = log.getLogType().name().equals("INFO");
                    label.getStyleClass().add(isInfo ? "log-text" : "log-error-text");

                    setGraphic(label);
                }
            }
        };
    }


    private synchronized void setFollowCaret(boolean followCaret) {
        this.followCaret = followCaret;
    }

    private synchronized boolean getFollowCaret() {
        return followCaret;
    }

    public ListView<LogWrapper> getComponent() {
        return logListView;
    }
}