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

	public void setAnchorConstraints(Node node, double top, double right, double bottom, double left) {
		if (node == null) {
			return;
		}
		if (scene == null) {
			return;
		}
		Runnable block1 = () -> {
			AnchorPane.setRightAnchor(node, scene.getWidth() * right - node.getBoundsInLocal().getWidth() / 2);
			AnchorPane.setLeftAnchor(node, scene.getWidth() * left - node.getBoundsInLocal().getWidth() / 2);
		};
		Runnable block2 = () -> {
			AnchorPane.setTopAnchor(node, scene.getHeight() * top - node.getBoundsInLocal().getHeight() / 2);
			AnchorPane.setBottomAnchor(node, scene.getHeight() * bottom - node.getBoundsInLocal().getHeight() / 2);
		};
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			block1.run();
		});
		scene.heightProperty().addListener((observable, oldValue, newValue) -> {
			block2.run();
		});
		block1.run();
		block2.run();
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
		Runnable block = () -> AnchorPane.setTopAnchor(node,
				scene.getHeight() * top - node.getBoundsInLocal().getHeight() / 2);
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

	public void setAnchorConstraintsSides(Node node, double right, double left) {
		if (node == null) {
			return;
		}
		if (scene == null) {
			return;
		}
		Runnable block = () -> {
			AnchorPane.setRightAnchor(node, scene.getWidth() * right - node.getBoundsInLocal().getWidth() / 2);
			AnchorPane.setLeftAnchor(node, scene.getWidth() * left - node.getBoundsInLocal().getWidth() / 2);
		};
		scene.widthProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}

	public void setAnchorConstraintsTopBottom(Node node, double top, double bottom) {
		if (node == null) {
			return;
		}
		if (scene == null) {
			return;
		}
		Runnable block = () -> {
			AnchorPane.setTopAnchor(node, scene.getHeight() * top - node.getBoundsInLocal().getHeight() / 2);
			AnchorPane.setBottomAnchor(node, scene.getHeight() * bottom - node.getBoundsInLocal().getHeight() / 2);
		};
		scene.heightProperty().addListener((observable, oldValue, newValue) -> {
			block.run();
		});
		block.run();
	}
}
