package org.nevertouchgrass.prolific.service;

import jakarta.annotation.PostConstruct;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@Scope("prototype")
public class AnchorPaneConstraintsService {
    private Stage stage;
    private Scene scene;

    public void setStage(Stage stage) {
        this.stage = stage;
        this.scene = stage.getScene();
    }

    AnchorPaneConstraintsService() {
        System.out.println("AnchorPaneConstraintsService created.");
    }

    @PostConstruct
    public void init() {
        System.out.println("AnchorPaneConstraintsService initialized.");
    }

    public void setAnchorConstraintsLeft(Node node, double left) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setLeftAnchor(node,
                stage.getWidth() * left - node.getBoundsInLocal().getWidth() / 2);
        stage.widthProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());
        block.run();
    }

    public void setAnchorConstraintsRight(Node node, double right) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setRightAnchor(node,
                stage.getWidth() * right - node.getBoundsInLocal().getWidth() / 2);
        stage.widthProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());

        block.run();
    }

    public void setAnchorConstraintsTop(Node node, double top) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setTopAnchor(node,
                stage.getHeight() * top - node.getBoundsInLocal().getHeight() / 2);
        stage.heightProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());

        block.run();
    }

    public void setAnchorConstraintsBottom(Node node, double bottom) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setBottomAnchor(node,
                stage.getHeight() * bottom - node.getBoundsInLocal().getHeight() / 2);
        stage.heightProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());

        block.run();
    }


    public void setAnchorConstraintsIgnoreElementSizeLeft(Node node, double left) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setLeftAnchor(node, stage.getWidth() * left);
        stage.widthProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());

        block.run();
    }

    public void setAnchorConstraintsIgnoreElementSizeRight(Node node, double right) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setRightAnchor(node, stage.getWidth() * right);
        stage.widthProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());

        block.run();
    }

    public void setAnchorConstraintsIgnoreElementSizeTop(Node node, double top) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setTopAnchor(node, stage.getHeight() * top);
        stage.heightProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());

        block.run();
    }

    public void setAnchorConstraintsIgnoreElementSizeBottom(Node node, double bottom) {
        if (node == null) {
            return;
        }
        Runnable block = () -> AnchorPane.setBottomAnchor(node, stage.getHeight() * bottom);
        stage.heightProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());
        block.run();
    }
}
