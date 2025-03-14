package org.nevertouchgrass.prolific.service.icons;

import javafx.scene.Group;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.stereotype.Component;

@Component
public class GradleProjectIconFactory extends AbstractIconFactory {
    public GradleProjectIconFactory() {
        super("gradle");
    }

    @Override
    public StackPane configure() {
        var svgPath = new SVGPath();
        svgPath.setContent(
                "M18.9016 2.6532C18.4322 1.6737 16.8861 0.516188 15.1539 2.02988M18.9016 2.6532C" +
                        "19.5006 3.79447 18.4726 5.92331 16.4004 5.92331C13.6672 5.92331 10.7983 1.34244 " +
                        "4.47909 4.59609C4.06186 4.81092 3.87557 5.30243 4.01951 5.7491L4.24258 " +
                        "6.44135M18.9016 2.6532C18.9016 2.6532 20.6308 6.29914 15.8839 " +
                        "9.85C14.5388 10.8573 14.0507 12.8398 14.0058 14.5986C14.0044 14.6532 13.9599 " +
                        "14.6968 13.9054 14.6968H12.3477C12.2486 14.6968 12.1656 14.6239 12.1423 " +
                        "14.5275C11.9362 13.6731 11.1678 13.0643 10.2705 13.0643C9.37546 13.0643 8.60879 " +
                        "13.6787 8.40024 14.5306C8.37674 14.6266 8.29385 14.6991 8.19504 " +
                        "14.6991H6.69933C6.60052 14.6991 6.51763 14.6266 6.49413 14.5306C6.28558 " +
                        "13.6787 5.51891 13.0643 4.62391 13.0643C3.72864 13.0643 2.96177 13.6791 2.75349 " +
                        "14.5314C2.73003 14.6274 2.64712 14.7 2.54828 14.7H0.926436C0.875473 14.7 0.832559 " +
                        "14.661 0.827236 14.6104C0.43905 10.9138 1.50445 8.17577 4.24258 6.44135M4.24258 " +
                        "6.44135L4.99534 8.91598C5.27503 9.73917 6.39897 9.8753 7.08481 " +
                        "9.49017C7.93671 9.02951 8.72209 8.48399 9.44376 7.83393"
        );
        svgPath.setStrokeWidth(1.5);
        svgPath.setStroke(Color.web("#CED0D6"));
        svgPath.setStrokeLineCap(StrokeLineCap.ROUND);
        svgPath.setFill(Color.TRANSPARENT);

        SVGPath svgPath2 = new SVGPath();
        svgPath2.setContent("M11.5569 7.51317C11.4617 7.45196 11.4054 7.34109 11.4369 7.23233C11.5518 6.83589 " +
                "11.8961 6.53859 12.3182 6.49271C12.8024 6.44009 13.2577 6.73263 13.4111 7.19497C13.5448 7.59798 " +
                "13.4175 8.03469 13.1046 8.3039C13.0188 8.37776 12.8946 8.37265 12.7993 8.31144L11.5569 7.51317Z");
        svgPath2.setFill(Color.TRANSPARENT);
        svgPath2.setStroke(Color.web("#CED0D6"));
        svgPath2.setFillRule(FillRule.EVEN_ODD);

        Group group = new Group(svgPath, svgPath2);

        return new StackPane(group);
    }
}
