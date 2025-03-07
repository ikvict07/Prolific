package org.nevertouchgrass.prolific.service;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * Service that manages constraints
 *
 * @see org.nevertouchgrass.prolific.annotation.Constraints
 * @see org.nevertouchgrass.prolific.annotation.ConstraintsIgnoreElementSize
 */
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

    private void setAnchorConstraint(Node node, BiConsumer<Node, Double> anchorSetter, double value, boolean isWidthBased) {
        if (node == null) {
            return;
        }
        var parent = node.getParent();
        if (!(parent instanceof Region region)) {
            return;
        }

        Runnable block = () -> anchorSetter.accept(node, (isWidthBased ? region.getWidth() : region.getHeight()) * value);
        addResizeListener(region, block, isWidthBased);
        addStageListeners(block);
        block.run();
    }

    private void setAnchorWithOffset(Node node, BiConsumer<Node, Double> anchorSetter, double value, boolean isWidthBased) {
        if (node == null) {
            return;
        }
        var parent = node.getParent();
        if (!(parent instanceof Region region)) {
            return;
        }
        Runnable block = () -> {
            double offset = (isWidthBased ? region.getWidth() : region.getHeight()) * value - node.getBoundsInLocal().getWidth() / 2;
            anchorSetter.accept(node, Math.max(0, offset));
        };

        addResizeListener(region, block, isWidthBased);
        addStageListeners(block);
        block.run();
    }

    private void addResizeListener(Region region, Runnable block, boolean isWidthBased) {
        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> Platform.runLater(block);
        if (isWidthBased) {
            region.widthProperty().addListener(resizeListener);
        } else {
            region.heightProperty().addListener(resizeListener);
        }
    }

    private void addStageListeners(Runnable block) {
        stage.widthProperty().addListener((_, _, _) -> block.run());
        stage.heightProperty().addListener((_, _, _) -> block.run());
        stage.maximizedProperty().addListener((_, _, _) -> block.run());
        stage.setOnShown(_ -> block.run());
    }

    public void setAnchorConstraintsLeft(Node node, double left) {
        setAnchorWithOffset(node, AnchorPane::setLeftAnchor, left, true);
    }

    public void setAnchorConstraintsRight(Node node, double right) {
        setAnchorWithOffset(node, AnchorPane::setRightAnchor, right, true);
    }

    public void setAnchorConstraintsTop(Node node, double top) {
        setAnchorWithOffset(node, AnchorPane::setTopAnchor, top, false);
    }

    public void setAnchorConstraintsBottom(Node node, double bottom) {
        setAnchorWithOffset(node, AnchorPane::setBottomAnchor, bottom, false);
    }

    public void setAnchorConstraintsIgnoreElementSizeLeft(Node node, double left) {
        setAnchorConstraint(node, AnchorPane::setLeftAnchor, left, true);
    }

    public void setAnchorConstraintsIgnoreElementSizeRight(Node node, double right) {
        setAnchorConstraint(node, AnchorPane::setRightAnchor, right, true);
    }

    public void setAnchorConstraintsIgnoreElementSizeTop(Node node, double top) {
        setAnchorConstraint(node, AnchorPane::setTopAnchor, top, false);
    }

    public void setAnchorConstraintsIgnoreElementSizeBottom(Node node, double bottom) {
        setAnchorConstraint(node, AnchorPane::setBottomAnchor, bottom, false);
    }

    public void setElementWidth(Region node, double width) {
        if (node == null) {
            return;
        }
        Runnable block = () -> {
            double newWidth = stage.getWidth() * width;
            node.setPrefWidth(newWidth);
            node.setMaxWidth(newWidth);
            node.setMinWidth(newWidth);
        };
        addStageListeners(block);
        block.run();
    }

    public void setElementHeight(Region node, double height) {
        if (node == null) {
            return;
        }
        Runnable block = () -> {
            double newHeight = stage.getHeight() * height;
            node.setPrefHeight(newHeight);
            node.setMaxHeight(newHeight);
            node.setMinHeight(newHeight);
        };
        addStageListeners(block);
        block.run();
    }
}
