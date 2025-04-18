package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;

import java.util.Arrays;
import java.util.Locale;

@StageComponent(stage = "settingsStage")
@Lazy
public class SettingsOptionGeneral extends AbstractSettingsOption {
    @FXML public StackPane rootPathChooser;

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

    @Setter(onMethod_ = @Autowired)
    private ApplicationEventPublisher applicationEventPublisher;

    private static final int RESCAN_MIN = 1;
    private static final int RESCAN_MAX = 72;
    private static final int MAX_SCAN_DEPTH_MIN = 1;
    private static final int MAX_SCAN_DEPTH_MAX = 30;

    @Initialize
    @SneakyThrows
    public void init() {
        fxmlProvider.getFxmlResource("settingsOptionGeneral");

        pathChooserLocalizationMap.put(rootPathChooser, localizationProvider.setting_root_path_to_scan());
        pathChooserPathSettingMap.put(rootPathChooser, rootPathSetting);

        setupValidators();
    }

    @Override
    public void setupValidators() {
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

        languageSetting.valueProperty().addListener((_, _, _) -> checkDefaultValues());

        rootPathSetting.setText(userSettingsHolder.getBaseScanDirectory());

        excludedDirsSetting.setText(String.join(";", userSettingsHolder.getExcludedDirs()));

        var supportedTranslations = userSettingsHolder.getSupportedTranslations().stream().map(it -> {
            var locale = Locale.forLanguageTag(it);
            return locale.getDisplayLanguage(locale);
        });
        languageSetting.getItems().clear();
        languageSetting.getItems().addAll(supportedTranslations.toList());

        var locale = userSettingsHolder.getLocale().getDisplayLanguage(userSettingsHolder.getLocale());
        languageSetting.getSelectionModel().select(locale);


        rescanEveryHoursSetting.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(RESCAN_MIN, RESCAN_MAX, userSettingsHolder.getRescanEveryHours()));

        maxScanDepthSetting.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MAX_SCAN_DEPTH_MIN, MAX_SCAN_DEPTH_MAX, userSettingsHolder.getMaximumProjectDepth()));
    }

    @Override
    public boolean validInput() {
        return checkProvidedPath(rootPathSetting.getText(), rootPathSetting, rootPathErrorMessage);
    }

    @Override
    public boolean checkDefaultValues() {
        boolean result = rootPathSetting.getText().toLowerCase().equals(userSettingsHolder.getBaseScanDirectory()) &&
                excludedDirsSetting.getText().equals(String.join(";", userSettingsHolder.getExcludedDirs())) &&
                rescanEveryHoursSetting.getValue().equals(userSettingsHolder.getRescanEveryHours()) &&
                maxScanDepthSetting.getValue().equals(userSettingsHolder.getMaximumProjectDepth()) &&
                userSettingsHolder.getLocale().getDisplayLanguage(userSettingsHolder.getLocale()).equals(languageSetting.getValue());

        settingsFooterController.changeApplyButtonStyle(result);

        return result;
    }

    @Override
    public boolean saveSettings() {
        if (validInput() && !checkDefaultValues()) {
            userSettingsHolder.setBaseScanDirectory(rootPathSetting.getText());
            userSettingsHolder.setExcludedDirs(Arrays.stream(excludedDirsSetting.getText().split(";")).toList());
            userSettingsHolder.setRescanEveryHours(rescanEveryHoursSetting.getValue());
            userSettingsHolder.setMaximumProjectDepth(maxScanDepthSetting.getValue());
            userSettingsHolder.setLocale(Locale.forLanguageTag(userSettingsHolder.getSupportedTranslations().get(languageSetting.getSelectionModel().getSelectedIndex())));

            userSettingsService.saveSettings();

            applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this, userSettingsHolder.getLocale()));
            return true;
        }

        return false;
    }

    private void setupSpinnerValidation(Spinner<Integer> spinner, SpinnerValueFactory<Integer> valueFactory, TextFormatter<Integer> formatter) {
        spinner.setValueFactory(valueFactory);
        spinner.getEditor().setTextFormatter(formatter);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((_, _, _) -> checkDefaultValues());
    }

    @Autowired
    public void setSettingsStage(Stage settingsStage) {
        this.settingsStage = settingsStage;
    }
}
