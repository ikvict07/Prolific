package org.nevertouchgrass.prolific.components;

import javafx.application.Platform;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.nevertouchgrass.prolific.model.Metric;
import org.nevertouchgrass.prolific.model.ProcessMetrics;
import org.nevertouchgrass.prolific.service.metrics.MetricsService;
import org.nevertouchgrass.prolific.util.ProcessWrapper;
import org.reactfx.EventSource;
import reactor.core.scheduler.Schedulers;

import java.util.List;

public class MetricsChartComponent extends VBox {
    private final XYChart.Series<Number, Number> cpuSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> memorySeries = new XYChart.Series<>();
    private final EventSource<Metric> metricEvents = new EventSource<>();
    private final ProcessMetrics processMetrics;
    private double maxCpuUsage = 100;
    private AreaChart<Number, Number> cpuChart;
    private AreaChart<Number, Number> memoryChart;
    private NumberAxis yCpuAxis;
    private NumberAxis yMemAxis;

    public MetricsChartComponent(MetricsService metricsService, ProcessWrapper process, ProcessMetrics processMetrics) {
        this.processMetrics = processMetrics;
        setMaxWidth(Double.MAX_VALUE);
        this.getStyleClass().add("metrics-chart");
        HBox.setHgrow(this, Priority.ALWAYS);
        VBox.setVgrow(this, Priority.ALWAYS);

        createCharts();

        setupMetricEventHandler();

        if (metricsService != null && process != null) {
            metricsService.subscribeToMetrics(process)
                    .publishOn(Schedulers.single())
                    .subscribe(metric -> {
                        Platform.runLater(() -> metricEvents.push(metric));
                    });
        }
    }

    public void init() {
        cpuSeries.getData().clear();
        memorySeries.getData().clear();

        if (processMetrics != null) {
            List<Metric> historicalMetrics = processMetrics.getMetrics();
            if (historicalMetrics != null && !historicalMetrics.isEmpty()) {
                double maxCpu = 0;
                double maxMem = 0;

                for (int i = 0; i < historicalMetrics.size(); i++) {
                    Metric metric = historicalMetrics.get(i);
                    double cpuValue = metric.getCpuUsage();
                    double memoryInMB = metric.getMemoryUsage() / (1024.0 * 1024.0);

                    cpuSeries.getData().add(new XYChart.Data<>(i, cpuValue));
                    memorySeries.getData().add(new XYChart.Data<>(i, memoryInMB));

                    maxCpu = Math.max(maxCpu, cpuValue);
                    maxMem = Math.max(maxMem, memoryInMB);
                }

                updateAxisScales(maxCpu, maxMem);
            }
        }
    }

    private void createCharts() {
        NumberAxis xCpuAxis = new NumberAxis("Time", 0, 60, 10);
        yCpuAxis = new NumberAxis("CPU %", 0, maxCpuUsage, 10);
        yCpuAxis.setAutoRanging(false);

        cpuChart = new AreaChart<>(xCpuAxis, yCpuAxis);
        cpuChart.getData().add(cpuSeries);
        cpuChart.setLegendVisible(false);
        cpuChart.setCreateSymbols(false);
        cpuChart.getStyleClass().add("chart-plot-background");
        cpuChart.setHorizontalGridLinesVisible(false);
        cpuChart.setVerticalGridLinesVisible(false);
        cpuChart.getStyleClass().add("filled-chart-green");
        cpuChart.setStyle("-fx-stroke: #89C398;");
        HBox.setHgrow(cpuChart, Priority.ALWAYS);

        NumberAxis xMemAxis = new NumberAxis("Time", 0, 60, 10);
        yMemAxis = new NumberAxis("RAM (MB)", 0, 1000, 100);
        yMemAxis.setAutoRanging(false);

        memoryChart = new AreaChart<>(xMemAxis, yMemAxis);
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
        metricEvents.observe(metric -> {
            long x = cpuSeries.getData().size();
            double cpuValue = metric.getCpuUsage();
            double memoryInMB = metric.getMemoryUsage() / (1024.0 * 1024.0);

            if (cpuValue > maxCpuUsage) {
                maxCpuUsage = cpuValue;
                if (maxCpuUsage > yCpuAxis.getUpperBound()) {
                    double newUpperBound = Math.ceil(maxCpuUsage * 1.2 / 10) * 10;
                    newUpperBound = Math.min(Runtime.getRuntime().availableProcessors() * 100, newUpperBound);
                    yCpuAxis.setUpperBound(newUpperBound);
                    yCpuAxis.setTickUnit(newUpperBound / 10);
                }
            }

            if (memoryInMB > yMemAxis.getUpperBound()) {
                double newUpperBound = Math.ceil(memoryInMB * 1.2 / 100) * 100;
                yMemAxis.setUpperBound(newUpperBound);
                yMemAxis.setTickUnit(newUpperBound / 10);
            }

            cpuSeries.getData().add(new XYChart.Data<>(x, cpuValue));
            memorySeries.getData().add(new XYChart.Data<>(x, memoryInMB));

            if (cpuSeries.getData().size() > 100) {
                cpuSeries.getData().remove(0);
                memorySeries.getData().remove(0);
            }
        });
    }

    private void updateAxisScales(double maxCpu, double maxMem) {
        if (maxCpu > 0) {
            maxCpuUsage = maxCpu;
            double newUpperBound = Math.ceil(maxCpuUsage * 1.2 / 10) * 10;
            newUpperBound = Math.min(Runtime.getRuntime().availableProcessors() * 100, newUpperBound);
            yCpuAxis.setUpperBound(newUpperBound);
            yCpuAxis.setTickUnit(newUpperBound / 10);
        }

        if (maxMem > 0) {
            double newUpperBound = Math.ceil(maxMem * 1.2 / 100) * 100;
            yMemAxis.setUpperBound(newUpperBound);
            yMemAxis.setTickUnit(newUpperBound / 10);
        }
    }

    public void clear() {
        cpuSeries.getData().clear();
        memorySeries.getData().clear();
    }
}