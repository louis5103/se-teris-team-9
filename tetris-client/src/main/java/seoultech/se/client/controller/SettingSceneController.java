package seoultech.se.client.controller;

import java.io.IOException;

import javafx.stage.Stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.javafx.scene.control.inputmap.InputMap;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import seoultech.se.client.service.NavigationService;
import seoultech.se.client.service.*;

@Component
public class SettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;
    @Autowired
    private SettingsService appSettingsService;
    @Autowired
    private KeyMappingService keyMappingService;

    @FXML
    private Slider soundSlider;  // slider의 properties에 listener 추가 필요
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
    private RadioButton colorModeDefault;
    @FXML
    private RadioButton colorModeRGBlind;
    @FXML
    private RadioButton colorModeYBlind;
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
        soundSlider.setValue(80);
        soundSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("🔊 Sound volume set to: " + newVal.intValue());
            //TODO : 사운드 볼륨 조절 기능 구현
        });
        screenSizeM.setSelected(true);
        colorModeDefault.setSelected(true);
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

        appSettingsService.applyResolution(width, height);
    }

    @FXML
    public void handleColorModeChange(ActionEvent event) {
        RadioButton selectedRadioButton = (RadioButton) event.getSource();
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
        soundSlider.setValue(80);
        screenSizeM.setSelected(true);
        colorModeDefault.setSelected(true);
        
        appSettingsService.applyResolution(500, 700);
        keyMappingService.resetToDefault();
        //TODO : 색약모드 초기화
        // appSettingsService.applyColorMode("default");
    }

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/main-view.fxml");
        //다른곳에서 setting으로 이동시에는 이전 페이지로 돌아가도록 수정 필요
    }
}