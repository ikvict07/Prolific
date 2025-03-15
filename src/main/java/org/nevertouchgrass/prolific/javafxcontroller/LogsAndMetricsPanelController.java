package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.springframework.stereotype.Component;

@Component
public class LogsAndMetricsPanelController {
    @FXML
    private TextArea logsAndMetrics;

    @FXML
    public void initialize() {
        logsAndMetrics.setText("a".repeat(1000) + "Hello\nWorld\nWtf\n".repeat(20));
    }
}
