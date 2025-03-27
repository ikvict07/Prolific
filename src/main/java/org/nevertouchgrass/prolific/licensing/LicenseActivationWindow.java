package org.nevertouchgrass.prolific.licensing;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.nevertouchgrass.prolific.javafxcontroller.SettingsDropdownController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



@Getter
@Component
public class LicenseActivationWindow {
    @Setter
    private boolean isActivated;

    public LicenseActivationWindow() {
        this.isActivated = false;
    }

    public boolean getIsActivated() {
        return isActivated;
    }

    public void showLicenseWindow(Stage primaryStage) {
        setActivated(false);
        Label titleLabel = new Label("Enter your license key");
        TextField licenseKeyField = new TextField();
        Button activateButton = new Button("Activate");

        activateButton.setOnAction(event -> {
            String inputKey = licenseKeyField.getText();
            boolean isValid = LicenseManager.verifyLicenseKey(inputKey);

            if (isValid) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "License activated successfully!");

                alert.setOnHidden(e -> primaryStage.close());
                setActivated(true);
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid license key.");
                alert.showAndWait();
            }
        });

        VBox layout = new VBox(10, titleLabel, licenseKeyField, activateButton);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 400, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("License Activation");
        primaryStage.show();
    }
}
