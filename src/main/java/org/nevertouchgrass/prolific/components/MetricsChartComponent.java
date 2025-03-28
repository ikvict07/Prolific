package org.nevertouchgrass.prolific.components;

import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.nevertouchgrass.prolific.model.Metric;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.reactfx.EventSource;
import reactor.core.scheduler.Schedulers;

import java.time.format.DateTimeFormatter;

public class MetricsChartComponent extends VBox {
    private final XYChart.Series<String, Number> cpuSeries = new XYChart.Series<>();
    private final XYChart.Series<String, Number> memorySeries = new XYChart.Series<>();
    private final EventSource<Metric> metricEvents = new EventSource<>();
    private double maxCpuUsage = 100;
    private NumberAxis yCpuAxis;
    private NumberAxis yMemAxis;
    private final long timeWindow = 20;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private CategoryAxis xCpuAxis;
    private CategoryAxis xMemAxis;

    public MetricsChartComponent(MetricsService metricsService, ProcessWrapper process) {
        setMaxWidth(Double.MAX_VALUE);
        this.getStyleClass().add("metrics-chart");
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);
        createCharts();

        setupMetricEventHandler();

        if (metricsService != null && process != null) {
            metricsService.subscribeToMetrics(process)
                    .publishOn(Schedulers.single())
                    .subscribe(metric -> Platform.runLater(() -> metricEvents.push(metric)));
        }
    }

    private void createCharts() {
        xCpuAxis = new CategoryAxis();
        yCpuAxis = new NumberAxis("CPU %", 0, maxCpuUsage, 50);
        yCpuAxis.setAutoRanging(false);

        AreaChart<String, Number> cpuChart = new AreaChart<>(xCpuAxis, yCpuAxis);
        cpuChart.getData().add(cpuSeries);
        cpuChart.setLegendVisible(false);
        cpuChart.setCreateSymbols(false);
        cpuChart.getStyleClass().add("chart-plot-background");
        cpuChart.setHorizontalGridLinesVisible(false);
        cpuChart.setVerticalGridLinesVisible(false);
        cpuChart.getStyleClass().add("filled-chart-green");
        cpuChart.setStyle("-fx-stroke: #89C398;");
        HBox.setHgrow(cpuChart, Priority.ALWAYS);
        xMemAxis = new CategoryAxis();
        yMemAxis = new NumberAxis("RAM (MB)", 0, 1000, 100);
        yMemAxis.setAutoRanging(false);

        AreaChart<String, Number> memoryChart = new AreaChart<>(xMemAxis, yMemAxis);
        memoryChart.getData().add(memorySeries);
        memoryChart.setLegendVisible(false);
        memoryChart.setCreateSymbols(false);
        memoryChart.getStyleClass().add("chart-plot-background");
        memoryChart.getStyleClass().add("filled-chart-blue");
        memoryChart.setHorizontalGridLinesVisible(false);
        memoryChart.setVerticalGridLinesVisible(false);
        memoryChart.setStyle("-fx-stroke: #A0BDF8;");
        HBox.setHgrow(memoryChart, Priority.ALWAYS);

        this.getChildren().addAll(cpuChart, memoryChart);
    }

    private void setupMetricEventHandler() {
        for (var i = 0; i < timeWindow; i++) {
            cpuSeries.getData().add(new XYChart.Data<>(" ".repeat(i), 0));
            memorySeries.getData().add(new XYChart.Data<>(" ".repeat(i), 0));
        }

        metricEvents.observe(metric -> {
            String formattedTime = metric.getTimeStamp().format(timeFormatter);
            double cpuValue = metric.getCpuUsage();
            double memoryInMB = metric.getMemoryUsage() / (1024.0 * 1024.0);
            XYChart.Data<String, Number> cpuData = new XYChart.Data<>(formattedTime, cpuValue);
            XYChart.Data<String, Number> memoryData = new XYChart.Data<>(formattedTime, memoryInMB);

            cpuSeries.getData().add(cpuData);
            memorySeries.getData().add(memoryData);

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

            while (cpuSeries.getData().size() > timeWindow) {
                cpuSeries.getData().remove(0);
                memorySeries.getData().remove(0);
            }
        });
    }

}