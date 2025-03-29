package org.nevertouchgrass.prolific.components;

import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ScrollBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.nevertouchgrass.prolific.model.Metric;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.reactfx.EventSource;
import reactor.core.scheduler.Schedulers;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MetricsChartComponent extends VBox {
    private final XYChart.Series<String, Number> cpuSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> memorySeries = new XYChart.Series<>();
    private final EventSource<Metric> metricEvents = new EventSource<>();
    private double maxCpuUsage = 100;
    private NumberAxis yCpuAxis;
    private NumberAxis yMemAxis;
    private final long timeWindow = 20;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final List<XYChart.Data<String, Number>> allCpuData = new ArrayList<>();
    private final List<XYChart.Data<String, Number>> allMemoryData = new ArrayList<>();

    private long viewStartIndex = 0;

    private ScrollBar scrollBar;

    public MetricsChartComponent(MetricsService metricsService, ProcessWrapper process) {
        setMaxWidth(Double.MAX_VALUE);
        this.getStyleClass().add("metrics-chart");
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        createCharts();
        createScrollBar();

        setupMetricEventHandler();


        if (metricsService != null && process != null) {
            metricsService.subscribeToMetrics(process)
                    .publishOn(Schedulers.single())
                    .subscribe(metric -> Platform.runLater(() -> metricEvents.push(metric)));
        }
    }

    private void createCharts() {
        CategoryAxis xCpuAxis = new CategoryAxis();
        yCpuAxis = new NumberAxis("CPU %", 0, maxCpuUsage, 50);
        yCpuAxis.setAutoRanging(false);

        AreaChart<String, Number> cpuChart = new AreaChart<>(xCpuAxis, yCpuAxis);
        cpuChart.getData().add(cpuSeries);
        cpuChart.setLegendVisible(false);
        cpuChart.setCreateSymbols(true);
        cpuChart.getStyleClass().add("chart-plot-background");
        cpuChart.setHorizontalGridLinesVisible(false);
        cpuChart.setVerticalGridLinesVisible(false);
        cpuChart.getStyleClass().add("filled-chart-green");
        cpuChart.setStyle("-fx-stroke: #89C398;");
        cpuChart.setAnimated(false);
        HBox.setHgrow(cpuChart, Priority.ALWAYS);

        setupChartTooltip(cpuChart, cpuSeries, "CPU: %.2f%%");

        CategoryAxis xMemAxis = new CategoryAxis();
        yMemAxis = new NumberAxis("RAM (MB)", 0, 1000, 100);
        yMemAxis.setAutoRanging(false);

        AreaChart<String, Number> memoryChart = new AreaChart<>(xMemAxis, yMemAxis);
        memoryChart.getData().add(memorySeries);
        memoryChart.setLegendVisible(false);
        memoryChart.setCreateSymbols(true);
        memoryChart.getStyleClass().add("chart-plot-background");
        memoryChart.getStyleClass().add("filled-chart-blue");
        memoryChart.setHorizontalGridLinesVisible(false);
        memoryChart.setVerticalGridLinesVisible(false);
        memoryChart.setStyle("-fx-stroke: #A0BDF8;");
        memoryChart.setAnimated(false);
        HBox.setHgrow(memoryChart, Priority.ALWAYS);

        setupChartTooltip(memoryChart, memorySeries, "Memory: %.2f MB");

        this.getChildren().addAll(cpuChart, memoryChart);
    }

    private void createScrollBar() {
        scrollBar = new ScrollBar();
        scrollBar.setVisible(false);
        scrollBar.getStyleClass().add("scroll-bar-metrics");

        scrollBar.setMin(0);
        scrollBar.setMax(100);
        scrollBar.setValue(scrollBar.getMax());


        scrollBar.setVisibleAmount(20);
        scrollBar.setUnitIncrement(1);
        scrollBar.setBlockIncrement(5);
        scrollBar.setOrientation(javafx.geometry.Orientation.HORIZONTAL);

        scrollBar.valueProperty().addListener((_, _, newVal) -> {
            int dataSize = allCpuData.size();

            long realDataStartIndex = timeWindow;

            if (dataSize <= timeWindow * 2) {
                updateChartView(realDataStartIndex);
                return;
            }

            long maxStartIndex = dataSize - timeWindow;

            double percentage = newVal.doubleValue() / 100.0;
            long newIndex = Math.round(percentage * (maxStartIndex - realDataStartIndex)) + realDataStartIndex;

            newIndex = Math.max(realDataStartIndex, Math.min(newIndex, maxStartIndex));

            updateChartView(newIndex);
        });

        HBox scrollBarBox = new HBox(scrollBar);
        HBox.setHgrow(scrollBar, Priority.ALWAYS);
        scrollBarBox.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        this.getChildren().add(scrollBarBox);
    }

    private void setupChartTooltip(AreaChart<String, Number> chart, XYChart.Series<String, Number> series, String valueFormat) {
        javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();
        tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        final javafx.animation.PauseTransition hoverTimer = new javafx.animation.PauseTransition(javafx.util.Duration.millis(200));
        final double[] lastMousePos = new double[2];

        chart.setOnMouseMoved(event -> {
            double currentX = event.getX();
            double currentY = event.getY();

            double deltaX = Math.abs(currentX - lastMousePos[0]);
            double deltaY = Math.abs(currentY - lastMousePos[1]);

            if (deltaX > 3 || deltaY > 3) {
                lastMousePos[0] = currentX;
                lastMousePos[1] = currentY;

                tooltip.hide();

                hoverTimer.setOnFinished(_ -> {
                    XYChart.Data<String, Number> closestData = findClosestDataPoint(chart, series, currentX);

                    if (closestData != null) {
                        String time = closestData.getXValue();
                        Number value = closestData.getYValue();
                        String tooltipText = time + "\n" + String.format(valueFormat, value.doubleValue());

                        tooltip.setText(tooltipText);

                        tooltip.show(chart, event.getScreenX() + 10, event.getScreenY() + 10);
                    }
                });

                hoverTimer.playFromStart();
            }
        });

        chart.setOnMouseExited(_ -> {
            tooltip.hide();
            hoverTimer.stop();
        });

    }


    private XYChart.Data<String, Number> findClosestDataPoint(AreaChart<String, Number> chart,
                                                              XYChart.Series<String, Number> series,
                                                              double mouseX) {
        javafx.geometry.Bounds plotAreaBounds = chart.lookup(".chart-plot-background").getBoundsInLocal();
        double plotMinX = plotAreaBounds.getMinX();
        double plotMaxX = plotAreaBounds.getMaxX();
        double plotWidth = plotMaxX - plotMinX;

        if (mouseX < plotMinX || mouseX > plotMaxX) {
            return null;
        }

        double positionRatio = (mouseX - plotMinX) / plotWidth;

        List<XYChart.Data<String, Number>> dataPoints = new ArrayList<>();
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (!data.getXValue().trim().isEmpty()) {
                dataPoints.add(data);
            }
        }

        if (dataPoints.isEmpty()) {
            return null;
        }

        int dataSize = dataPoints.size();
        int index = Math.min(Math.max(0, (int) (positionRatio * dataSize)), dataSize - 1);

        return dataPoints.get(index);
    }

    private void setupMetricEventHandler() {
        for (var i = 0; i < timeWindow; i++) {
            XYChart.Data<String, Number> cpuData = new XYChart.Data<>(" ".repeat(i), 0);
            XYChart.Data<String, Number> memData = new XYChart.Data<>(" ".repeat(i), 0);

            allCpuData.add(cpuData);
            allMemoryData.add(memData);

            cpuSeries.getData().add(cpuData);
            memorySeries.getData().add(memData);
        }

        metricEvents.observe(metric -> {
            if (allCpuData.size() > timeWindow * 2) {
                scrollBar.setVisible(true);
            }
            String formattedTime = metric.getTimeStamp().format(timeFormatter);
            double cpuValue = metric.getCpuUsage();
            double memoryInMB = metric.getMemoryUsage() / (1024.0 * 1024.0);

            XYChart.Data<String, Number> cpuData = new XYChart.Data<>(formattedTime, cpuValue);
            XYChart.Data<String, Number> memoryData = new XYChart.Data<>(formattedTime, memoryInMB);

            allCpuData.add(cpuData);
            allMemoryData.add(memoryData);

            boolean isAtEnd = (scrollBar.getValue() >= 95) ||
                    (viewStartIndex >= allCpuData.size() - timeWindow - 2);


            if (isAtEnd) {
                viewStartIndex = Math.max(0, allCpuData.size() - timeWindow);
                updateChartView(viewStartIndex);
            } else {
                updateChartView(viewStartIndex);
            }

            if (cpuValue > maxCpuUsage) {
                maxCpuUsage = cpuValue;
                yCpuAxis.setUpperBound(Math.min(
                        Runtime.getRuntime().availableProcessors() * 100d + 10,
                        Math.ceil(maxCpuUsage * 1.2 / 10) * 10 + 10
                ));
            }

            if (memoryInMB > yMemAxis.getUpperBound()) {
                yMemAxis.setUpperBound(Math.ceil(memoryInMB * 1.2 / 100) * 100);
            }
        });
    }

    private void updateChartView(long startIndex) {
        viewStartIndex = startIndex;

        cpuSeries.getData().clear();
        memorySeries.getData().clear();

        long endIndex = Math.min(startIndex + timeWindow, allCpuData.size());

        for (int i = (int) startIndex; i < endIndex; i++) {
            cpuSeries.getData().add(allCpuData.get(i));
            memorySeries.getData().add(allMemoryData.get(i));
        }
    }

}
