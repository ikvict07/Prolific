package org.nevertouchgrass.prolific.service.icons;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultProjectTypeIconFactory extends AbstractIconFactory {
    public DefaultProjectTypeIconFactory() {
        super("default");
    }

    @Override
    public StackPane configure() {
        SVGPath svgPath1 = new SVGPath();
        svgPath1.setContent("M3.12642 3.16655C3.32168 2.97129 3.63827 2.97129 3.83353 3.16655L6.16698 5.5L3.83353 " +
                "7.83345C3.63827 8.02871 3.32168 8.02871 3.12642 7.83345C2.93116 7.63819 2.93116 7.32161 3.12642 " +
                "7.12635L4.75277 5.5L3.12642 3.87365C2.93116 3.67839 2.93116 3.36181 3.12642 3.16655Z");
        svgPath1.setFill(Color.web("#CED0D6"));

        SVGPath svgPath2 = new SVGPath();
        svgPath2.setContent("M6.5 8C6.22386 8 6 8.22386 6 8.5C6 8.77614 6.22386 9 6.5 9H9.5C9.77614 9 10 8.77614 10 " +
                "8.5C10 8.22386 9.77614 8 9.5 8L6.5 8Z");
        svgPath2.setFill(Color.web("#CED0D6"));

        SVGPath svgPath3 = new SVGPath();
        svgPath3.setContent("M2 0C0.895431 0 0 0.895431 0 2V10C0 11.1046 0.89543 12 2 12H12C13.1046 12 14 11.1046 14 " +
                "10V2C14 0.895431 13.1046 0 12 0H2ZM12 1H2C1.44772 1 1 1.44772 1 2V10C1 10.5523 1.44772 11 2 " +
                "11H12C12.5523 11 13 10.5523 13 10V2C13 1.44772 12.5523 1 12 1Z");
        svgPath3.setFill(Color.web("#CED0D6"));
        svgPath3.setFillRule(FillRule.EVEN_ODD);

        Group group = new Group(svgPath3, svgPath2, svgPath1);
        group.setScaleX(1.5);
        group.setScaleY(1.5);

        return new StackPane(group);
    }
}