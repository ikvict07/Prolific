package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Locale;

@StageComponent(stage = "settingsStage")
public class SettingsListController {
    @Setter(onMethod_ = {@Qualifier("settingsStage"), @Autowired})
    private Stage settingsStage;
    @Setter(onMethod_ = @Autowired)
    private UserSettingsHolder userSettingsHolder;
    @Setter(onMethod_ = @Autowired)
    private SettingsFooterController settingsFooterController;

    @FXML public Label rootPath;
    @FXML public Label excludedDirs;
    @FXML public Label maxScanDepth;
    @FXML public Label rescanEveryHours;
    @FXML public Label language;
    @FXML public TextField rootPathSetting;
    @FXML public TextField excludedDirsSetting;
    @FXML public Spinner<Integer> rescanEveryHoursSetting;
    @FXML public ComboBox<String> supportedTranslationsSetting;
    @FXML public Spinner<Integer> maxScanDepthSetting;
    @FXML public Label general;

    @FXML
    public void initialize() {
        int rescanMin = 1, rescanMax = 72;
        int maxScanDepthMin = 1, maxScanDepthMax = 30;

        TextFormatter<Integer> rescanEveryHoursFormatter = new TextFormatter<>(new IntegerStringConverter(),
                userSettingsHolder.getRescanEveryHours(), it -> createIntegerChange(it, rescanMin, rescanMax));
        TextFormatter<Integer> maxScanDepthFormatter = new TextFormatter<>(new IntegerStringConverter(),
                userSettingsHolder.getMaximumProjectDepth(), it -> createIntegerChange(it, maxScanDepthMin, maxScanDepthMax));

        SpinnerValueFactory<Integer> rescanEveryHoursValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(rescanMin, rescanMax, userSettingsHolder.getRescanEveryHours());
        rescanEveryHoursSetting.setValueFactory(rescanEveryHoursValueFactory);
        rescanEveryHoursSetting.getEditor().setTextFormatter(rescanEveryHoursFormatter);
        rescanEveryHoursSetting.setEditable(true);
        rescanEveryHoursSetting.valueProperty().addListener((observable, oldValue, newValue) -> {
            checkDefaultValues();
        });


        SpinnerValueFactory<Integer> maxScanDepthValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(maxScanDepthMin, maxScanDepthMax, userSettingsHolder.getMaximumProjectDepth());
        maxScanDepthSetting.setValueFactory(maxScanDepthValueFactory);
        maxScanDepthSetting.getEditor().setTextFormatter(maxScanDepthFormatter);
        maxScanDepthSetting.setEditable(true);
        maxScanDepthSetting.valueProperty().addListener((observable, oldValue, newValue) -> {
            checkDefaultValues();
        });

        var supportedTranslations = userSettingsHolder.getSupportedTranslations().stream().map(it -> {
            var locale = Locale.forLanguageTag(it);
            return locale.getDisplayLanguage(locale);
        });
        supportedTranslationsSetting.getItems().addAll(supportedTranslations.toList());
        supportedTranslationsSetting.valueProperty().addListener(
                (_, _, newValue) -> {
                    checkDefaultValues();
                }
        );

        rootPathSetting.textProperty().addListener(
                (_, _, newValue) ->
                        checkDefaultValues()
        );

        excludedDirsSetting.textProperty().addListener((_, _, newValue) -> {
                    checkDefaultValues();
                }
        );

        settingsStage.setOnShowing(_ -> {
            rootPathSetting.setText(userSettingsHolder.getBaseScanDirectory());

            excludedDirsSetting.setText(String.join(";", userSettingsHolder.getExcludedDirs()));

            supportedTranslationsSetting.getSelectionModel().select(userSettingsHolder.getLocale().getDisplayLanguage());
        });

    }

    public void handleMouseEntered() {
        settingsStage.getScene().setCursor(Cursor.DEFAULT);
    }

    private void checkDefaultValues() {
        settingsFooterController.changeApplyButtonStyle(rootPathSetting.getText().toLowerCase().equals(userSettingsHolder.getBaseScanDirectory()) &&
                excludedDirsSetting.getText().equals(String.join(";", userSettingsHolder.getExcludedDirs())) &&
                rescanEveryHoursSetting.getValue().equals(userSettingsHolder.getRescanEveryHours()) &&
                maxScanDepthSetting.getValue().equals(userSettingsHolder.getMaximumProjectDepth()) &&
                userSettingsHolder.getLocale().getDisplayLanguage().equals(supportedTranslationsSetting.getValue()));
    }

    private TextFormatter.Change createIntegerChange(TextFormatter.Change change, int min, int max) {
        String newText = change.getControlNewText();
        if (newText.matches("\\d*")) {
            try {
                if (!newText.isEmpty()) {
                    var value = Integer.parseInt(newText);
                    if (value < min) {
                        change.setText(String.valueOf(min));
                        change.setRange(0, change.getControlText().length());
                    } else if (value > max) {
                        change.setText(String.valueOf(max));
                        change.setRange(0, change.getControlText().length());
                    }
                } else {
                    change.setText(String.valueOf(min));
                    change.setRange(0, change.getControlText().length());
                }
            } catch (NumberFormatException e) {
                return null;
            }
            return change;
        }
        return null;
    }
}
