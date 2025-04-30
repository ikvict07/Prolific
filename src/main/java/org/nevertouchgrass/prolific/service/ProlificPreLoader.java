package org.nevertouchgrass.prolific.service;

import javafx.application.Preloader;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class ProlificPreLoader extends Preloader {
    @Override
    @SneakyThrows
    public void start(Stage primaryStage) {
        com.sun.glass.ui.Application.GetApplication().setName("Prolific");
    }
}
