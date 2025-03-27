package org.nevertouchgrass.prolific.components;

import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
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

public class MetricsChartComponent extends VBox {
    private final XYChart.Series<Number, Number> cpuSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> memorySeries = new XYChart.Series<>();
    private final EventSource<Metric> metricEvents = new EventSource<>();
    private double maxCpuUsage = 100;

    public MetricsChartComponent(MetricsService metricsService, ProcessWrapper process) {
        setMaxWidth(Double.MAX_VALUE);
        this.getStyleClass().add("metrics-chart");
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        NumberAxis xCpuAxis = new NumberAxis("Time", 0, 60, 10);
        NumberAxis yCpuAxis = new NumberAxis("CPU %", 0,  maxCpuUsage, 10);
        yCpuAxis.setAutoRanging(false);
        var cpuChart = new AreaChart<>(xCpuAxis, yCpuAxis);
        cpuChart.getData().add(cpuSeries);
        cpuChart.setLegendVisible(false);
        cpuChart.setCreateSymbols(false);
        cpuChart.getStyleClass().add("chart-plot-background");
        cpuChart.setHorizontalGridLinesVisible(false);
        cpuChart.setVerticalGridLinesVisible(false);
        cpuChart.getStyleClass().add("filled-chart-green");
        NumberAxis xMemAxis = new NumberAxis("Time", 0, 60, 10);
        NumberAxis yMemAxis = new NumberAxis("RAM (MB)", 0, 1000, 100);
        var memoryChart = new AreaChart<>(xMemAxis, yMemAxis);
        memoryChart.getData().add(memorySeries);
        memoryChart.setLegendVisible(false);
        memoryChart.setCreateSymbols(false);
        memoryChart.getStyleClass().add("chart-plot-background");
        memoryChart.getStyleClass().add("filled-chart-blue");
        memoryChart.setHorizontalGridLinesVisible(false);
        memoryChart.setVerticalGridLinesVisible(false);
        this.getChildren().addAll(cpuChart, memoryChart);
        cpuChart.setStyle("-fx-stroke: #89C398;");
        memoryChart.setStyle("-fx-stroke: #A0BDF8;");

        metricEvents.observe(metric -> {
            long x = cpuSeries.getData().size();
            double cpuValue = metric.getCpuUsage();

            if (cpuValue > maxCpuUsage) {
                maxCpuUsage = cpuValue;
                if (maxCpuUsage > yCpuAxis.getUpperBound()) {
                    double newUpperBound = Math.ceil(maxCpuUsage * 1.2 / 10) * 10;
                    newUpperBound = Math.min(Runtime.getRuntime().availableProcessors() * 100, newUpperBound);
                    yCpuAxis.setUpperBound(newUpperBound);
                    yCpuAxis.setTickUnit(newUpperBound / 10);
                }
            }

            cpuSeries.getData().add(new XYChart.Data<>(x, cpuValue));

            double memoryInMB = metric.getMemoryUsage() / (1024.0 * 1024.0);
            memorySeries.getData().add(new XYChart.Data<>(x, memoryInMB));

            if (cpuSeries.getData().size() > 100) {
                cpuSeries.getData().remove(0);
                memorySeries.getData().remove(0);
            }
        });

        metricsService.subscribeToMetrics(process)
                .publishOn(Schedulers.single())
                .subscribe(metric -> {
                    Platform.runLater(() -> metricEvents.push(metric));
                });
    }
}