package org.nevertouchgrass.prolific.javafxcontroller.settings.options;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.events.LocalizationChangeEvent;
import org.nevertouchgrass.prolific.service.settings.PathService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

@StageComponent(stage = "settingsStage")
@Lazy
@Log4j2
public class SettingsOptionGeneral extends AbstractSettingsOption {
    @FXML
    public StackPane rootPathChooser;

    @FXML
    public Label rootPath;
    @FXML
    public Label excludedDirs;
    @FXML
    public Label maxScanDepth;
    @FXML
    public Label rescanEveryHours;
    @FXML
    public Label language;

    @FXML
    public Label rootPathErrorMessage;

    @FXML
    public TextField rootPathSetting;
    @FXML
    public TextField excludedDirsSetting;
    @FXML
    public Spinner<Integer> rescanEveryHoursSetting;
    @FXML
    public Spinner<Integer> maxScanDepthSetting;
    @FXML
    public ComboBox<String> languageSetting;
    @FXML
    public Label exportLabel;
    @FXML
    public Label importLabel;

    @Setter(onMethod_ = @Autowired)
    private PathService pathService;
    @Setter(onMethod_ = @Autowired)
    private ApplicationEventPublisher applicationEventPublisher;

    private static final int RESCAN_MIN = 1;
    private static final int RESCAN_MAX = 72;
    private static final int MAX_SCAN_DEPTH_MIN = 1;
    private static final int MAX_SCAN_DEPTH_MAX = 30;

    private boolean isInitialized = false;

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
        if (!isInitialized) {
            initializeUIComponents();
            isInitialized = true;
        }
        updateUIValues();

    }

    private void updateUIValues() {
        rootPathSetting.setText(userSettingsHolder.getBaseScanDirectory());
        excludedDirsSetting.setText(String.join(";", userSettingsHolder.getExcludedDirs()));

        rescanEveryHoursSetting.getValueFactory().setValue(userSettingsHolder.getRescanEveryHours());
        maxScanDepthSetting.getValueFactory().setValue(userSettingsHolder.getMaximumProjectDepth());

        var supportedTranslations = userSettingsHolder.getSupportedTranslations().stream().map(it -> {
            var locale = Locale.forLanguageTag(it);
            return locale.getDisplayLanguage(locale);
        });
        languageSetting.getItems().clear();
        languageSetting.getItems().addAll(supportedTranslations.toList());

        var locale = userSettingsHolder.getLocale().getDisplayLanguage(userSettingsHolder.getLocale());
        languageSetting.getSelectionModel().select(locale);
    }

    private void initializeUIComponents() {
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

    @Override
    public void resetToDefaults() {
        rootPathSetting.setText(userSettingsHolder.getBaseScanDirectory());
        excludedDirsSetting.setText(String.join(";", userSettingsHolder.getExcludedDirs()));
        rescanEveryHoursSetting.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(RESCAN_MIN, RESCAN_MAX, userSettingsHolder.getRescanEveryHours()));
        maxScanDepthSetting.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MAX_SCAN_DEPTH_MIN, MAX_SCAN_DEPTH_MAX, userSettingsHolder.getMaximumProjectDepth()));
        languageSetting.getSelectionModel().select(userSettingsHolder.getLocale().getDisplayLanguage(userSettingsHolder.getLocale()));
    }

    private void setupSpinnerValidation(Spinner<Integer> spinner, SpinnerValueFactory<Integer> valueFactory, TextFormatter<Integer> formatter) {
        spinner.setValueFactory(valueFactory);
        spinner.getEditor().setTextFormatter(formatter);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((_, _, _) -> checkDefaultValues());
    }

    @Autowired
    public void setSettingsStage(@Qualifier("settingsStage") Stage settingsStage) {
        this.stage = settingsStage;
    }


    public void export() {
        DirectoryChooser fileChooser = new DirectoryChooser();
        fileChooser.setTitle("Select Export Directory");
        fileChooser.setInitialDirectory(pathService.getProjectFilesPath().toFile());
        try {
            String f = fileChooser.showDialog(stage).getPath();
            Path p = Path.of(f).toRealPath(LinkOption.NOFOLLOW_LINKS);
            String fileName = getFileName();
            p = p.resolve(fileName);
            userSettingsService.saveSettingsTo(p);
        } catch (Exception e) {
            log.error("Error exporting settings", e);
        }
    }

    private String getFileName() {
        TextInputDialog fileNameDialog = new TextInputDialog(pathService.getSettingsName());
        fileNameDialog.setTitle("Export Settings");
        fileNameDialog.setHeaderText(null);
        fileNameDialog.setContentText("Filename:");

        DialogPane dialogPane = fileNameDialog.getDialogPane();
        dialogPane.setGraphic(null);

        dialogPane.getStylesheets().add(Objects.requireNonNull(SettingsOptionGeneral.class.getResource("/css/styles.css")).toExternalForm());
        dialogPane.getStyleClass().clear();
        dialogPane.getStyleClass().add("dialog-pane");
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.getStyleClass().clear();
        okButton.getStyleClass().add("settings-submit-button");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.getStyleClass().clear();
        cancelButton.getStyleClass().add("settings-cancel-button");


        TextField textField = fileNameDialog.getEditor();
        textField.getStyleClass().add("log-text");


        String fileName = fileNameDialog.showAndWait().orElse(pathService.getSettingsName());
        if (fileName.isEmpty()) {
            fileName = pathService.getSettingsName();
        }
        if (!fileName.endsWith(".xml")) {
            fileName += ".xml";
        }
        return fileName;
    }

    public void importSettings() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Import File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files", "*.xml"));
        fileChooser.setInitialDirectory(pathService.getProjectFilesPath().toFile());
        try {
            String f = fileChooser.showOpenDialog(stage).getPath();
            Path p = Path.of(f).toRealPath(LinkOption.NOFOLLOW_LINKS);
            userSettingsService.loadSettingsFrom(p);
            applicationEventPublisher.publishEvent(new LocalizationChangeEvent(this, userSettingsHolder.getLocale()));
            stage.close();
        } catch (Exception e) {
            log.error("Error importing settings", e);
        }}
}
