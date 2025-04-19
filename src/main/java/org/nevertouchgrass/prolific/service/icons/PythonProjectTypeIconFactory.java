package org.nevertouchgrass.prolific.service.icons;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.stereotype.Component;

@Component
public class PythonProjectTypeIconFactory extends AbstractIconFactory {
    public PythonProjectTypeIconFactory() {
        super("python");
    }

    @Override
    public StackPane configure() {
        StackPane stackPane = new StackPane();

        var group = new Group();
        SVGPath blueShape = new SVGPath();
        blueShape.setContent("M8.00001 1C11 1 11 2 11 4L11 6.5C11 7.32843 10.3284 8 9.5 8H6.5C5.11929 8 4 9.11929 4 10.5V11C2 11 1 11 1 7.99999C1 4.99999 2 4.99998 4 4.99998L7.5 5C7.77614 5 8 4.77614 8 4.5C8 4.22386 7.77614 4 7.5 4H5.00001C5.00001 2 5.00001 1 8.00001 1ZM6.5 3C6.77614 3 7 2.77614 7 2.5C7 2.22386 6.77614 2 6.5 2C6.22386 2 6 2.22386 6 2.5C6 2.77614 6.22386 3 6.5 3Z");
        blueShape.setFill(Color.web("#548AF7"));

        SVGPath yellowShape = new SVGPath();
        yellowShape.setContent("M12 5V6.5C12 7.88071 10.8807 9 9.5 9H6.5C5.67157 9 5 9.67157 5 10.5L5.00001 12C4.99946 14 5.00001 15 8.00001 15C11 15 11 14 11 12L8.5 12C8.22386 12 8 11.7761 8 11.5C8 11.2239 8.22386 11 8.5 11L12 11C14 11.0005 15 11 15 7.99999C15 5.00002 14 5.00001 12 5ZM9.5 14C9.77614 14 10 13.7761 10 13.5C10 13.2239 9.77614 13 9.5 13C9.22386 13 9 13.2239 9 13.5C9 13.7761 9.22386 14 9.5 14Z");
        yellowShape.setFill(Color.web("#F2C55C"));
        group.getChildren().addAll(blueShape, yellowShape);
        stackPane.getChildren().add(group);
        return stackPane;
    }
}
