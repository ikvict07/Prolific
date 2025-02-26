package org.nevertouchgrass.prolific.service;

import jakarta.annotation.PostConstruct;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@Scope("prototype")
public class AnchorPaneConstraintsService {
	private Scene scene;

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
				scene.getWidth() * left - node.getBoundsInLocal().getWidth() / 2);
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}

	public void setAnchorConstraintsRight(Node node, double right) {
		if (node == null) {
			return;
		}
		Runnable block = () -> AnchorPane.setRightAnchor(node,
				scene.getWidth() * right - node.getBoundsInLocal().getWidth() / 2);
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}

	public void setAnchorConstraintsTop(Node node, double top) {
		if (node == null) {
			return;
		}
		Runnable block = () -> {
			System.out.println("Setting top anchor for " + node);
			System.out.println("Scene height: " + scene.getHeight());
			System.out.println("Top: " + top);
			System.out.println("Node bounds: " + node.getBoundsInLocal());
			AnchorPane.setTopAnchor(node,
					scene.getHeight() * top - node.getBoundsInLocal().getHeight() / 2);
		};
		scene.heightProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}

	public void setAnchorConstraintsBottom(Node node, double bottom) {
		if (node == null) {
			return;
		}
		Runnable block = () -> AnchorPane.setBottomAnchor(node,
				scene.getHeight() * bottom - node.getBoundsInLocal().getHeight() / 2);
		scene.heightProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}


	public void setAnchorConstraintsIgnoreElementSizeLeft(Node node, double left) {
		if (node == null) {
			return;
		}
		Runnable block = () -> AnchorPane.setLeftAnchor(node, scene.getWidth() * left);
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}
	public void setAnchorConstraintsIgnoreElementSizeRight(Node node, double right) {
		if (node == null) {
			return;
		}
		Runnable block = () -> AnchorPane.setRightAnchor(node, scene.getWidth() * right);
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}
	public void setAnchorConstraintsIgnoreElementSizeTop(Node node, double top) {
		if (node == null) {
			return;
		}
		Runnable block = () -> AnchorPane.setTopAnchor(node, scene.getHeight() * top);
		scene.heightProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}
	public void setAnchorConstraintsIgnoreElementSizeBottom(Node node, double bottom) {
		if (node == null) {
			return;
		}
		Runnable block = () -> AnchorPane.setBottomAnchor(node, scene.getHeight() * bottom);
		scene.heightProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}
}
