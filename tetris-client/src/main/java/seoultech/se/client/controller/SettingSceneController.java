package seoultech.se.client.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import seoultech.se.client.service.NavigationService;

@Component
public class SettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;

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
    public void handleScreenSizeChange(ActionEvent event) {
        //구현 필요
    }

    @FXML
    public void handleColorModeChange(ActionEvent event) {
        //구현 필요
    }

    @FXML
    public void handleClearScoreBoardButton(ActionEvent event) {
        System.out.println("🧹 Clear Score Board button clicked");
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
    }

    @FXML
    public void handleBackButton(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/main-view.fxml");
        //다른곳에서 setting으로 이동시에는 이전 페이지로 돌아가도록 수정 필요
    }
}
