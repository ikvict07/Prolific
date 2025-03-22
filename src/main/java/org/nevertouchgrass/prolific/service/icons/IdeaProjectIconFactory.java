package org.nevertouchgrass.prolific.service.icons;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.stereotype.Component;

@Component
public class IdeaProjectIconFactory extends AbstractIconFactory {
    public IdeaProjectIconFactory() {
        super("idea");
    }

    @Override
    public StackPane configure() {
        StackPane stackPane = new StackPane();

        SVGPath blueSquare = new SVGPath();
        stackPane.getChildren().add(blueSquare);
        blueSquare.setContent("M16 0H0V16H16V0Z");
        blueSquare.setFill(Color.web("#087CFA"));
        StackPane.setAlignment(blueSquare, Pos.CENTER);
        blueSquare.setScaleX(1.2);
        blueSquare.setScaleY(1.2);

        SVGPath blackSquare = new SVGPath();
        stackPane.getChildren().add(blackSquare);
        blackSquare.setContent("M15 1H1V15H15V1Z");
        blackSquare.setFill(Color.BLACK);
        StackPane.setAlignment(blackSquare, Pos.CENTER);
        blackSquare.setScaleX(1.2);
        blackSquare.setScaleY(1.2);

        SVGPath whiteLine = new SVGPath();
        stackPane.getChildren().add(whiteLine);
        whiteLine.setContent("M9 12H3V13H9V12Z");
        whiteLine.setFill(Color.WHITE);
        StackPane.setAlignment(whiteLine, Pos.CENTER);
        whiteLine.setTranslateY(6);
        whiteLine.setTranslateX(-2);
        whiteLine.setScaleX(1.2);
        whiteLine.setScaleY(1.2);

        SVGPath whiteIconPart1 = new SVGPath();
        stackPane.getChildren().add(whiteIconPart1);
        whiteIconPart1.setContent("M6.0125 4.09998V3.00623H3.03125V4.09998H3.86875V7.87498H3.03125V8.96873H6.0125V7.87498H5.18125V4.09998H6.0125Z");
        whiteIconPart1.setFill(Color.WHITE);
        StackPane.setAlignment(whiteIconPart1, Pos.CENTER);
        whiteIconPart1.setTranslateX(-3);
        whiteIconPart1.setScaleX(1.2);
        whiteIconPart1.setScaleY(1.2);

        SVGPath whiteIconPart2 = new SVGPath();
        stackPane.getChildren().add(whiteIconPart2);
        whiteIconPart2.setContent("M8.8748 9.05623C8.40605 9.05623 8.01855 8.96873 7.70605 8.79373C7.39355 8.61873 7.13105 8.40623 6.9248 8.16248L7.7498 7.24373C7.91855 7.43123 8.09355 7.57498 8.2623 7.68123C8.4373 7.78748 8.6248 7.83748 8.8373 7.83748C9.0873 7.83748 9.28105 7.76248 9.4248 7.59998C9.56855 7.44373 9.6373 7.19373 9.6373 6.84373V3.00623H10.9811V6.90623C10.9811 7.26248 10.9311 7.57498 10.8373 7.83748C10.7436 8.09998 10.5998 8.31873 10.4186 8.49998C10.2373 8.68123 10.0123 8.81873 9.7498 8.91248C9.49355 9.00623 9.1998 9.05623 8.8748 9.05623Z");
        whiteIconPart2.setFill(Color.WHITE);
        StackPane.setAlignment(whiteIconPart2, Pos.CENTER);
        whiteIconPart2.setTranslateX(1);
        whiteIconPart2.setScaleX(1.2);
        whiteIconPart2.setScaleY(1.2);

        return stackPane;
    }

}
