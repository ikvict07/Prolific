package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.nevertouchgrass.prolific.licensing.HttpClientConfig;


import java.util.Map;

public class LicenseController {
    @FXML
    public TextField nameField;
    @FXML
    public Label yourNameLabel;
    @FXML
    public Button createLicenseButton;
    @FXML
    public Button changeLicenseButton;
    @FXML
    public Button createLicenseButtonSecondary;
    @FXML
    private TextField licenseField;
    @FXML
    private Button activateLicenseButton;
    @FXML
    private Label licenseLabel;
    @FXML
    private Button activateButton;
    private String licenseKey;
    @FXML
    private Button createButton;

    private static final String SERVICE_URL = "http://localhost:8081";

    @FXML
    private void initialize() {
        createLicenseButton.setVisible(false);
        nameField.setVisible(false);
        createLicenseButtonSecondary.setVisible(false);
        changeLicenseButton.setDisable(true);
        yourNameLabel.setStyle("-fx-visibility: visible;");
        yourNameLabel.setText("Your name: " + System.getProperty("user.name"));

        if (!HttpClientConfig.isBackendAvailable(SERVICE_URL)) {
            licenseLabel.setText("⚠ License server is unavailable");
            licenseLabel.setStyle("-fx-text-fill: red;");
            licenseLabel.setVisible(true);

            activateButton.setDisable(true);
            activateLicenseButton.setDisable(true);
            createButton.setDisable(true);
            changeLicenseButton.setDisable(true);
            licenseField.setVisible(false);
            nameField.setVisible(false);
            return;
        }

        try {
            String userId = System.getProperty("user.name");
            ResponseEntity<String> response = HttpClientConfig.postJson("http://localhost:8081/is-activated", Map.of("userId", userId), null);
            boolean activated = Boolean.parseBoolean(response.getBody());

            if (activated) {
                licenseLabel.setText("✔ License already activated");
                licenseLabel.setStyle("-fx-text-fill: green;");
                licenseLabel.setVisible(true);
                licenseField.setVisible(false);
                nameField.setDisable(true);
                activateButton.setDisable(true);
                createButton.setDisable(true);
                activateLicenseButton.setDisable(true);
                changeLicenseButton.setDisable(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void activateLicenseButtonClicked() {
        try {
            String licenseKey = licenseField.getText();
            String userId = System.getProperty("user.name");

            ResponseEntity<String> response = HttpClientConfig.postJson(
                    "http://localhost:8081/verify",
                    Map.of("userId", userId, "licenseKey", licenseKey),
                    null
            );

            boolean valid = Boolean.parseBoolean(response.getBody());
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
    private void createLicense() {
        try {
            String userId = System.getProperty("user.name");

            ResponseEntity<String> response = HttpClientConfig.postJson(
                    "http://localhost:8081/create",
                    Map.of("userId", userId),
                    "SECRET_TOKEN"
            );

            ObjectMapper mapper = new ObjectMapper();
            licenseKey = mapper.readTree(response.getBody()).get("licenseKey").asText();

            licenseLabel.setText("✔ License created: " + licenseKey);
            licenseLabel.setVisible(true);

            createLicenseButton.setDisable(true);
            createButton.setDisable(true);
            nameField.setDisable(true);
        } catch (Exception e) {
            licenseLabel.setText("⚠ Error creating license");
            licenseLabel.setStyle("-fx-text-fill: orange;");
            licenseLabel.setVisible(true);
            e.printStackTrace();
        }
    }

    @FXML
    private void createNewLicenseButtonClicked() {
        activateLicenseButton.setVisible(false);
        createLicenseButton.setVisible(true);
        licenseField.setVisible(false);
        licenseLabel.setText("Your license: Not activated");
        licenseLabel.setStyle("-fx-visibility: visible;");
    }

    @FXML
    private void activateLicenseField() {
        licenseField.setVisible(true);
        if (licenseKey != null)
            licenseField.setText(licenseKey);
        activateLicenseButton.setVisible(true);
        licenseField.setDisable(false);
        activateLicenseButton.setDisable(false);
        createLicenseButton.setVisible(false);
        licenseLabel.setText("");
        licenseLabel.setStyle("-fx-visibility: hidden;");
        createLicenseButtonSecondary.setVisible(false);
    }

    @FXML
    private void displayChangeLicenseButton() {
        licenseField.setVisible(false);
        activateButton.setDisable(false);
        activateLicenseButton.setVisible(false);
        createLicenseButton.setVisible(false);
        createLicenseButtonSecondary.setVisible(true);
        licenseLabel.setText("Create a new license?");
        licenseLabel.setStyle("-fx-visibility: visible;");
    }

    @FXML
    private void changeLicense() {
        try {
            String userId = System.getProperty("user.name");

            ResponseEntity<String> response = HttpClientConfig.postJson(
                    "http://localhost:8081/create",
                    Map.of("userId", userId),
                    "SECRET_TOKEN"
            );

            ObjectMapper mapper = new ObjectMapper();
            licenseKey = mapper.readTree(response.getBody()).get("licenseKey").asText();

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
