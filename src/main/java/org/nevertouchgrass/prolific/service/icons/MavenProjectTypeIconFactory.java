package org.nevertouchgrass.prolific.service.icons;

import javafx.scene.Group;
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
        SVGPath path = new SVGPath();
        path.setContent("M13.1621 0.201198C15.2238 0.201198 15.582 1.93587 15.2238 3.49891L13.8964 9.5117C13.801 " +
                "9.94408 13.3555 10.2209 12.9015 10.13C12.4476 10.0391 12.1569 9.61487 12.2524 9.1825L13.5034 " +
                "3.49891C14.0739 1.02039 10.839 1.32608 9.52649 3.16652L8.11076 9.51612C8.0153 9.94849 7.56989 10.2253 " +
                "7.1159 10.1344C7.09727 10.1307 7.07892 10.1264 7.06087 10.1215C6.6287 10.0153 6.35674 9.60273 6.44951 " +
                "9.1825L7.7054 3.49494C7.96685 2.37585 7.42991 1.82551 6.65007 1.7291C5.72182 1.62955 4.4504 2.17597 " +
                "3.74084 3.17093L3.7399 3.16422L3.73837 3.16652L2.31807 9.52227C2.22161 9.95444 1.77556 10.2303 1.32178 " +
                "10.1385C0.867995 10.0466 0.578324 9.62178 0.674778 9.18961L2.5029 0.998514C2.59936 0.566341 3.04541 " +
                "0.290464 3.49919 0.382325C4.01496 0.486735 4.22722 0.959731 4.12783 1.41342C5.07731 0.665356 6.18382 " +
                "0.136832 7.44866 0.206329C8.6834 0.23114 9.28702 0.894621 9.48446 1.76249C10.6229 0.789273 11.5848 " +
                "0.201198 13.1621 0.201198Z");
        path.setFill(Color.web("#548AF7"));

        Group group = new Group(path);
        group.setScaleX(1.2);
        group.setScaleY(1.2);

        return new StackPane(group);

    }
}
