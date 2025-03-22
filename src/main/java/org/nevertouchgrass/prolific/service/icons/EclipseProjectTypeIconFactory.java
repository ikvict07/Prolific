package org.nevertouchgrass.prolific.service.icons;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.nevertouchgrass.prolific.service.icons.contract.AbstractIconFactory;
import org.springframework.stereotype.Component;

@Component
public class EclipseProjectTypeIconFactory extends AbstractIconFactory {
    public EclipseProjectTypeIconFactory() {
        super("eclipse");
    }

    @Override
    public StackPane configure() {
        StackPane stackPane = new StackPane();

        SVGPath path1 = new SVGPath();
        stackPane.getChildren().add(path1);
        path1.setContent("M1.64039 9.8277C1.64039 4.86154 5.3702 0.735189 10.2223 0.00941938C10.1019 0.0049963 9.98097 0 9.85943 0C4.41443 0 0 4.4002 0 9.8277C0 15.2554 4.41423 19.6552 9.85943 19.6552C9.98136 19.6552 10.1023 19.6508 10.2231 19.6462C5.3702 18.9204 1.64039 14.7941 1.64039 9.8277Z");
        path1.setFill(Color.web("#F7941E"));
        StackPane.setAlignment(path1, Pos.CENTER);
        path1.setTranslateX(-6);

        SVGPath path2 = new SVGPath();
        stackPane.getChildren().add(path2);
        path2.setContent("M20.6369 7.11403C20.2077 5.64749 19.4108 4.33057 18.2444 3.16403C16.7812 1.70095 15.0736 0.822873 13.1242 0.527488C12.6311 0.45268 12.1233 0.413452 11.5988 0.413452C9.0023 0.413452 6.78172 1.33057 4.93537 3.16403C3.77672 4.33057 2.98518 5.64749 2.55902 7.11403");
        path2.setFill(Color.web("#2C2255"));
        StackPane.setAlignment(path2, Pos.CENTER);
        path2.setTranslateX(1);
        path2.setTranslateY(-6);

        SVGPath path3 = new SVGPath();
        stackPane.getChildren().add(path3);
        path3.setContent("M5.74197 7.11401H2.55888C2.45427 7.4569 2.38677 7.80863 2.32196 8.16729H3.96139H5.35735H17.7956H19.5572H20.8162C20.7512 7.80902 20.6676 7.45728 20.5631 7.11401");
        path3.setFill(Color.WHITE);
        StackPane.setAlignment(path3, Pos.CENTER);
        path3.setTranslateX(1);
        path3.setTranslateY(-2);

        SVGPath path5 = new SVGPath();
        stackPane.getChildren().add(path5);
        path5.setContent("M4.2535 10.3538H5.19273H18.0208H19.706H20.9862C20.9951 10.1863 21.0001 10.0177 21.0001 9.84768C21.0001 9.66403 20.9933 9.48211 20.9829 9.30057H19.7058H18.0206H5.19254H4.18696H2.19946C2.18907 9.48172 2.18234 9.66403 2.18234 9.84768C2.18234 10.0177 2.18734 10.1863 2.19619 10.3538H4.2535Z");
        path5.setFill(Color.web("#2C2255"));
        StackPane.setAlignment(path5, Pos.CENTER);
        path5.setTranslateX(1);

        SVGPath path4 = new SVGPath();
        stackPane.getChildren().add(path4);
        path4.setContent("M4.2535 10.3538H5.19273H18.0208H19.706H20.9862C20.9951 10.1863 21.0001 10.0177 21.0001 9.84768C21.0001 9.66403 20.9933 9.48211 20.9829 9.30057H19.7058H18.0206H5.19254H4.18696H2.19946C2.18907 9.48172 2.18234 9.66403 2.18234 9.84768C2.18234 10.0177 2.18734 10.1863 2.19619 10.3538H4.2535Z");
        path4.setFill(Color.WHITE);
        StackPane.setAlignment(path4, Pos.CENTER);
        path4.setTranslateX(1);
        path4.setTranslateY(0);

        SVGPath path6 = new SVGPath();
        stackPane.getChildren().add(path6);
        path6.setContent("M2.19937 10.3538C2.21841 10.7409 2.26014 11.1183 2.32187 11.4877H4.07072H5.3963H19.5594H20.8759C20.9379 11.1183 20.9802 10.7409 20.9994 10.3538");
        path6.setFill(Color.web("#2C2255"));
        StackPane.setAlignment(path6, Pos.CENTER);
        path6.setTranslateX(1);
        path6.setTranslateY(1);

        SVGPath path7 = new SVGPath();
        stackPane.getChildren().add(path7);
        path7.setContent("M19.607 11.4875H17.8454H5.40736H4.07813H2.32178C2.38485 11.8458 2.45659 12.1973 2.55947 12.5406H5.79159H17.4612H19.377H20.6247C20.7274 12.1977 20.8103 11.8461 20.8735 11.4875H19.607Z");
        path7.setFill(Color.WHITE);
        StackPane.setAlignment(path7, Pos.CENTER);
        path7.setTranslateX(1.5);
        path7.setTranslateY(2);

        SVGPath path8 = new SVGPath();
        stackPane.getChildren().add(path8);
        path8.setContent("M5.77957 12.5406H2.55957C2.98592 14.0031 3.77707 15.3141 4.93534 16.4723C6.78188 18.3192 9.00227 19.2412 11.5988 19.2412C12.1179 19.2412 12.6207 19.2025 13.1092 19.1289C15.065 18.8337 16.7773 17.9494 18.2444 16.4723C19.4102 15.3144 20.2069 14.0031 20.6365 12.5406H19.3298H17.419H5.77957Z");
        path8.setFill(Color.web("#2C2255"));
        StackPane.setAlignment(path8, Pos.CENTER);
        path8.setTranslateX(1);
        path8.setTranslateY(6);

        return stackPane;
    }
}
