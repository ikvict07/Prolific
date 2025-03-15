package org.nevertouchgrass.prolific.service.icons;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.stereotype.Component;

@Component
public class MavenProjectTypeIconFactory extends AbstractIconFactory {
    public MavenProjectTypeIconFactory() {
        super("maven");
    }

    @Override
    public StackPane configure() {
        StackPane stackPane = new StackPane();

        SVGPath path = new SVGPath();
        path.setContent("M13.1621 3.2012C15.2238 3.2012 15.582 4.93587 15.2238 6.49891L13.8964 12.5117C13.801 12.9441 13.3555 13.2209 12.9015 13.13C12.4476 13.0391 12.1569 12.6149 12.2524 12.1825L13.5034 6.49891C14.0739 4.02039 10.839 4.32608 9.52649 6.16652L8.11076 12.5161C8.0153 12.9485 7.56989 13.2253 7.1159 13.1344C7.09727 13.1307 7.07892 13.1264 7.06087 13.1215C6.6287 13.0153 6.35674 12.6027 6.44951 12.1825L7.7054 6.49494C7.96685 5.37585 7.42991 4.82551 6.65007 4.7291C5.72182 4.62955 4.4504 5.17597 3.74084 6.17093L3.7399 6.16422L3.73837 6.16652L2.31807 12.5223C2.22161 12.9544 1.77556 13.2303 1.32178 13.1385C0.867995 13.0466 0.578324 12.6218 0.674778 12.1896L2.5029 3.99851C2.59936 3.56634 3.04541 3.29046 3.49919 3.38233C4.01496 3.48674 4.22722 3.95973 4.12783 4.41342C5.07731 3.66536 6.18382 3.13683 7.44866 3.20633C8.6834 3.23114 9.28702 3.89462 9.48446 4.76249C10.6229 3.78927 11.5848 3.2012 13.1621 3.2012Z");
        path.setFill(Color.web("#548AF7"));
        path.setScaleX(1.5);
        path.setScaleY(1.5);
        stackPane.getChildren().add(path);
        StackPane.setAlignment(path, Pos.CENTER);
        return stackPane;
    }
}
