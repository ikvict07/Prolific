package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.nevertouchgrass.prolific.licensing.HttpClientConfig;
import java.util.Map;

public class LicenseController {
    @FXML
    public TextField nameField;
    @FXML
    public Label yourNameLabel;
    @FXML
    public Button createLicBut;
    @FXML
    public Button changeLic;
    @FXML
    public Button createLicBut1;
    @FXML
    private TextField licenseField;
    @FXML
    private Button activateLicBut;
    @FXML
    private Label licenseLabel;
    @FXML
    private Button activateBut;
    private String licenseKey;
    @FXML
    private Button createBut;
    @FXML
    private void initialize() {
    createLicBut.setVisible(false);
    nameField.setVisible(false);
    createLicBut1.setVisible(false);
    changeLic.setDisable(true);
    yourNameLabel.setStyle("-fx-visibility: visible;");
    yourNameLabel.setText("Your name: " + System.getProperty("user.name"));
        try {
            String userId = System.getProperty("user.name");
            var response = HttpClientConfig.postJson("http://localhost:8081/is-activated", Map.of("userId", userId), null);
            boolean activated = Boolean.parseBoolean(response.body());

            if (activated) {
                licenseLabel.setText("✔ License already activated");
                licenseLabel.setStyle("-fx-text-fill: green;");
                licenseLabel.setVisible(true);
                licenseField.setVisible(false);
                nameField.setDisable(true);
                activateBut.setDisable(true);
                createBut.setDisable(true);
                activateLicBut.setDisable(true);
                changeLic.setDisable(false);
            }
        } catch (Exception e) {
            licenseLabel.setText("⚠ Failed to check activation");
            e.printStackTrace();
        }
    }

    @FXML
    private void activateBut() {
            try {
                String licenseKey = licenseField.getText();
                String userId = System.getProperty("user.name");

                var response = HttpClientConfig.postJson(
                        "http://localhost:8081/verify",
                        Map.of("userId", userId, "licenseKey", licenseKey),
                        null
                );

                boolean valid = Boolean.parseBoolean(response.body());
                if (valid) {
                    licenseLabel.setText("✔ License activated!");
                    licenseLabel.setStyle("-fx-text-fill: " + "green" + ";");
                    licenseLabel.setVisible(true);
                    licenseField.setVisible(false);
                }
            } catch (Exception e) {
                licenseLabel.setText("⚠ Error checking license");
                licenseLabel.setStyle("-fx-text-fill: orange;");
                licenseLabel.setVisible(true);
                e.printStackTrace();
            }
        }

        @FXML
        private void createLic(){
            try {
            String userId = System.getProperty("user.name");

            var response = HttpClientConfig.postJson(
                    "http://localhost:8081/create",
                    Map.of("userId", userId),
                    "SECRET_TOKEN"
            );

            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            licenseKey = mapper.readTree(response.body()).get("licenseKey").asText();

            licenseLabel.setText("✔ License created: " + licenseKey);
            licenseLabel.setVisible(true);

            createLicBut.setDisable(true);
            createBut.setDisable(true);
            nameField.setDisable(true);
        } catch (Exception e) {
            licenseLabel.setText("⚠ Error creating license");
            licenseLabel.setStyle("-fx-text-fill: orange;");
            licenseLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void createBut() {
        activateLicBut.setVisible(false);
        createLicBut.setVisible(true);
        licenseField.setVisible(false);
        licenseLabel.setText("Your license: Not activated");
        licenseLabel.setStyle("-fx-visibility: visible;");
    }

    @FXML
    private void activateLic(){
        licenseField.setVisible(true);
        if (licenseKey != null)
            licenseField.setText(licenseKey);
        activateLicBut.setVisible(true);
        licenseField.setDisable(false);
        activateLicBut.setDisable(false);
        createLicBut.setVisible(false);
        licenseLabel.setText("");
        licenseLabel.setStyle("-fx-visibility: hidden;");
//        changeLic.setDisable(true);
        createLicBut1.setVisible(false);
    }
    @FXML
    private void setChangeLicBut(){
        licenseField.setVisible(false);
        activateBut.setDisable(false);
        activateLicBut.setVisible(false);
        createLicBut.setVisible(false);
        createLicBut1.setVisible(true);
        licenseLabel.setText("Create a new license?");
        licenseLabel.setStyle("-fx-visibility: visible;");
    }
    @FXML
    private void setChangeLic() {
        try {
            String userId = System.getProperty("user.name");

            var response = HttpClientConfig.postJson(
                    "http://localhost:8081/create",
                    Map.of("userId", userId),
                    "SECRET_TOKEN"
            );

            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            licenseKey = mapper.readTree(response.body()).get("licenseKey").asText();

            licenseLabel.setText("✔ License created: " + licenseKey);
            licenseLabel.setVisible(true);

        } catch (Exception e) {
            licenseLabel.setText("⚠ Error creating license");
            licenseLabel.setStyle("-fx-text-fill: orange;");
            licenseLabel.setVisible(true);
            e.printStackTrace();
        }
    }
}
