package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.nevertouchgrass.prolific.model.UserSettingsHolder;
import org.nevertouchgrass.prolific.model.notification.InfoNotification;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.notification.NotificationService;
import org.nevertouchgrass.prolific.service.settings.UserSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;

@StageComponent(stage = "settingsStage")
public class SettingsListController {
    @Setter(onMethod_ = {@Qualifier("settingsStage"), @Autowired})
    private Stage settingsStage;
    @Setter(onMethod_ = @Autowired)
    private UserSettingsHolder userSettingsHolder;
    @Setter(onMethod_ = @Autowired)
    private SettingsFooterController settingsFooterController;
    @Setter(onMethod_ = @Autowired)
    private ApplicationEventPublisher applicationEventPublisher;
    @Setter(onMethod_ = @Autowired)
    private UserSettingsService userSettingsService;
    @Setter(onMethod_ = @Autowired)
    private NotificationService notificationService;
    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;

    @FXML public Label general;
    @FXML public Label rootPath;
    @FXML public Label excludedDirs;
    @FXML public Label maxScanDepth;
    @FXML public Label rescanEveryHours;
    @FXML public Label language;

    @FXML public Label rootPathErrorMessage;

    @FXML public TextField rootPathSetting;
    @FXML public TextField excludedDirsSetting;
    @FXML public Spinner<Integer> rescanEveryHoursSetting;
    @FXML public Spinner<Integer> maxScanDepthSetting;
    @FXML public ComboBox<String> languageSetting;

    private static final String ERROR = "error";
    private static final int RESCAN_MIN = 1;
    private static final int RESCAN_MAX = 72;
    private static final int MAX_SCAN_DEPTH_MIN = 1;
    private static final int MAX_SCAN_DEPTH_MAX = 30;

    @FXML
    public void initialize() {
        settingsFooterController.setSaveRunnable(this::saveSettings);

        setupValidators();

        settingsStage.setOnShowing(_ -> {
            rootPathSetting.setText(userSettingsHolder.getBaseScanDirectory());
            excludedDirsSetting.setText(String.join(";", userSettingsHolder.getExcludedDirs()));

            var locale = userSettingsHolder.getLocale().getDisplayLanguage(userSettingsHolder.getLocale());
            languageSetting.getSelectionModel().select(locale);

            SpinnerValueFactory<Integer> rescanEveryHoursValueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(RESCAN_MIN, RESCAN_MAX, userSettingsHolder.getRescanEveryHours());
            rescanEveryHoursSetting.setValueFactory(rescanEveryHoursValueFactory);

            SpinnerValueFactory<Integer> maxScanDepthValueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(MAX_SCAN_DEPTH_MIN, MAX_SCAN_DEPTH_MAX, userSettingsHolder.getMaximumProjectDepth());
            maxScanDepthSetting.setValueFactory(maxScanDepthValueFactory);

            validateInput();
            checkDefaultValues();
        });
    }

    public void handleMouseEntered() {
        settingsStage.getScene().setCursor(Cursor.DEFAULT);
    }

    private boolean checkDefaultValues() {
        boolean result = rootPathSetting.getText().toLowerCase().equals(userSettingsHolder.getBaseScanDirectory()) &&
                excludedDirsSetting.getText().equals(String.join(";", userSettingsHolder.getExcludedDirs())) &&
                rescanEveryHoursSetting.getValue().equals(userSettingsHolder.getRescanEveryHours()) &&
                maxScanDepthSetting.getValue().equals(userSettingsHolder.getMaximumProjectDepth()) &&
                userSettingsHolder.getLocale().getDisplayLanguage(userSettingsHolder.getLocale()).equals(languageSetting.getValue());

        settingsFooterController.changeApplyButtonStyle(result);

        return result;
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

    private boolean validateInput() {
        return checkProvidedPath(rootPathSetting.getText());
    }

    private boolean checkProvidedPath(String path) {
        try {
            var result = Files.exists(Paths.get(path));
            if (result) {
                rootPathSetting.getStyleClass().remove(ERROR);
                rootPathErrorMessage.setVisible(false);
                rootPathErrorMessage.setManaged(false);
            } else {
                if (!rootPathSetting.getStyleClass().contains(ERROR)) {
                    rootPathSetting.getStyleClass().add(ERROR);
                }
                rootPathErrorMessage.setVisible(true);
                rootPathErrorMessage.setManaged(true);
            }
            return result;
        } catch (Exception ignore) {
            return false;
        }
    }

    private void saveSettings() {
        if (!checkDefaultValues() && validateInput()) {
            userSettingsHolder.setBaseScanDirectory(rootPathSetting.getText());
            userSettingsHolder.setExcludedDirs(Arrays.stream(excludedDirsSetting.getText().split(";")).toList());
            userSettingsHolder.setRescanEveryHours(rescanEveryHoursSetting.getValue());
            userSettingsHolder.setMaximumProjectDepth(maxScanDepthSetting.getValue());
            userSettingsHolder.setLocale(Locale.forLanguageTag(userSettingsHolder.getSupportedTranslations().get(languageSetting.getSelectionModel().getSelectedIndex())));

            userSettingsService.saveSettings();
            applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this, userSettingsHolder.getLocale()));
            notificationService.notifyInfo(InfoNotification.of(localizationProvider.settings_saved()));
            settingsFooterController.changeApplyButtonStyle(true);
        }
    }

    private void setupValidators() {
        TextFormatter<Integer> rescanEveryHoursFormatter = new TextFormatter<>(new IntegerStringConverter(),
                userSettingsHolder.getRescanEveryHours(), it -> createIntegerChange(it, RESCAN_MIN, RESCAN_MAX));
        TextFormatter<Integer> maxScanDepthFormatter = new TextFormatter<>(new IntegerStringConverter(),
                userSettingsHolder.getMaximumProjectDepth(), it -> createIntegerChange(it, MAX_SCAN_DEPTH_MIN, MAX_SCAN_DEPTH_MAX));

        SpinnerValueFactory<Integer> rescanEveryHoursValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(RESCAN_MIN, RESCAN_MAX, userSettingsHolder.getRescanEveryHours());
        setupSpinnerValidation(rescanEveryHoursSetting, rescanEveryHoursValueFactory, rescanEveryHoursFormatter);

        SpinnerValueFactory<Integer> maxScanDepthValueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(MAX_SCAN_DEPTH_MIN, MAX_SCAN_DEPTH_MAX, userSettingsHolder.getMaximumProjectDepth());
        setupSpinnerValidation(maxScanDepthSetting, maxScanDepthValueFactory, maxScanDepthFormatter);

        rootPathSetting.textProperty().addListener(
                (_, _, _) -> {
                    checkDefaultValues();
                    rootPathSetting.getStyleClass().remove(ERROR);
                    rootPathErrorMessage.setVisible(false);
                    rootPathErrorMessage.setManaged(false);
                }
        );

        excludedDirsSetting.textProperty().addListener((_, _, _) -> checkDefaultValues());

        var supportedTranslations = userSettingsHolder.getSupportedTranslations().stream().map(it -> {
            var locale = Locale.forLanguageTag(it);
            return locale.getDisplayLanguage(locale);
        });
        languageSetting.getItems().addAll(supportedTranslations.toList());
        languageSetting.valueProperty().addListener((_, _, _) -> checkDefaultValues());
    }

    private void setupSpinnerValidation(Spinner<Integer> spinner, SpinnerValueFactory<Integer> valueFactory, TextFormatter<Integer> formatter) {
        spinner.setValueFactory(valueFactory);
        spinner.getEditor().setTextFormatter(formatter);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((_, _, _) -> checkDefaultValues());
    }
}
