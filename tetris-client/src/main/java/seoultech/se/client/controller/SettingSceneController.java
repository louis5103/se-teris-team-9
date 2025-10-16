package seoultech.se.client.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import seoultech.se.client.config.ApplicationContextProvider;
import seoultech.se.client.service.KeyMappingService;
import seoultech.se.client.service.NavigationService;

@Component
public class SettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;
    @Autowired
    private KeyMappingService keyMappingService;

    @FXML
    private Slider soundSlider;
    @FXML
    private RadioButton screenSizeXS;
    @FXML
    private RadioButton screenSizeS;
    @FXML
    private RadioButton screenSizeM;
    @FXML
    private RadioButton screenSizeL;
    @FXML
    private RadioButton screenSizeXL;
    @FXML
    private RadioButton difficultyEasy;
    @FXML
    private RadioButton difficultyNormal;
    @FXML
    private RadioButton difficultyHard;
    @FXML
    private RadioButton colorModeDefault;
    @FXML
    private RadioButton colorModeRGBlind;
    @FXML
    private RadioButton colorModeBYBlind;
    @FXML
    private Button keySettingButton;
    @FXML
    private Button clearScoreBoardButton;
    @FXML
    private Button customSettingButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button backButton;

    @FXML
    @Override
    public void initialize() {
        super.initialize();

        this.settingsService = ApplicationContextProvider.getApplicationContext().getBean(seoultech.se.client.service.SettingsService.class);

        loadSettingsToUI();

        soundSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("🔊 Sound volume set to: " + newVal.intValue());
            settingsService.soundVolumeProperty().setValue(newVal.intValue());
            settingsService.saveSettings();
            //TODO : 사운드 볼륨 조절 기능 구현
        });
    }

    private void loadSettingsToUI() {
        settingsService.loadSettings();

        soundSlider.setValue(settingsService.soundVolumeProperty().getValue());
        String screenSize = settingsService.screenSizeProperty().getValue();
        String colorMode = settingsService.colorModeProperty().getValue();

        switch (screenSize) {
            case "screenSizeXS":
                screenSizeXS.setSelected(true);
                break;
            case "screenSizeS":
                screenSizeS.setSelected(true);
                break;
            case "screenSizeM":
                screenSizeM.setSelected(true);
                break;
            case "screenSizeL":
                screenSizeL.setSelected(true);
                break;
            case "screenSizeXL":
                screenSizeXL.setSelected(true);
                break;
            default:
                System.out.println("❗ Unknown screen size in settings: " + screenSize);
        }

        switch (colorMode) {
            case "colorModeDefault":
                colorModeDefault.setSelected(true);
                break;
            case "colorModeRGBlind":
                colorModeRGBlind.setSelected(true);
                break;
            case "colorModeBYBlind":
                colorModeBYBlind.setSelected(true);
                break;
            default:
                System.out.println("❗ Unknown color mode in settings: " + colorMode);
        }
    }

    @FXML
    public void handleScreenSizeChange(ActionEvent event) {
        RadioButton selectedRadioButton = (RadioButton) event.getSource();
        Stage stage = (Stage) selectedRadioButton.getScene().getWindow();

        double width = 500;
        double height = 700;

        //TODO : 해상도 hardcoding 제거
        switch (selectedRadioButton.getId()) {
            case "screenSizeXS":
                width = 300;
                height = 500;
                break;
            case "screenSizeS":
                width = 400;
                height = 600;
                break;
            case "screenSizeM":
                width = 500;
                height = 700;
                break;
            case "screenSizeL":
                width = 600;
                height = 800;
                break;
            case "screenSizeXL":
                width = 700;
                height = 900;
                break;
            default:
                System.out.println("❗ Unknown screen size selected");
        }
        settingsService.screenSizeProperty().setValue(selectedRadioButton.getId());
        settingsService.applyResolution(width, height);
        settingsService.saveSettings();
        System.out.println("🖥️ Screen size set to: " + selectedRadioButton.getId());
    }

    @FXML
    public void handleDifficultyChange(ActionEvent event) {
        // 난이도 변경 기능 구현 필요
    }

    @FXML
    public void handleColorModeChange(ActionEvent event) {
        RadioButton selectedRadioButton = (RadioButton) event.getSource();
        settingsService.colorModeProperty().setValue(selectedRadioButton.getId());
        settingsService.saveSettings();

        switch (selectedRadioButton.getId()) {
            case "colorModeDefault":
                System.out.println("🎨 Color mode set to: Default");
                //TODO : 색약모드 해제 기능 구현
                // settingsService.applyColorMode("default");
                break;
            case "colorModeRGBlind":
                System.out.println("🎨 Color mode set to: Red-Green Blindness");
                //TODO : 적녹색약 모드 적용 기능 구현
                // settingsService.applyColorMode("rgblind");
                break;
            case "colorModeBYBlind":
                System.out.println("🎨 Color mode set to: Blue-Yellow Blindness");
                //TODO : 황색약 모드 적용 기능 구현
                // settingsService.applyColorMode("yblind");
                break;
            default:
                System.out.println("❗ Unknown color mode selected");
        }
    }

    @FXML
    public void handleClearScoreBoardButton(ActionEvent event) {
        System.out.println("🧹 Clear Score Board button clicked");
        //TODO : 스코어보드 초기화 기능 구현
    }

    @FXML
    public void handleCustomSettingButton(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/custom-setting-view.fxml");
    }

    public void handleKeySettingButton(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/key-setting-view.fxml");
    }

    @FXML
    public void handleResetButton(ActionEvent event) {
        System.out.println("🔄 Reset all settings to default");
        settingsService.restoreDefaults();
        keyMappingService.resetToDefault();
        loadSettingsToUI();
    }

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/main-view.fxml");
        //다른곳에서 setting으로 이동시에는 이전 페이지로 돌아가도록 수정 필요
    }
}
