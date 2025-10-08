package seoultech.se.client.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import seoultech.se.client.service.NavigationService;

@Component
public class CustomSettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;

    @FXML
    private Button testSettingButton; //동적으로 생성한 버튼
    @FXML
    private Button backButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;
    @FXML
    private VBox settingContainer; // 동적으로 생성한 custom setting들을 담을 컨테이너

    @FXML
    private void handleTestSettingButton() { //동적으로 생성한 버튼들 대한 핸들러
        /*구현 필요
        삭제를 위해 특정 세팅을 의미하는 버튼을 클릭하면
        .custom-setting-button-selected 스타일을 적용시킴
        (선택 됐다는 의미에서 버튼색 파란색으로 변경) */
    }
    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/setting-view.fxml");
    }
    @FXML
    private void handleSaveButton() {
        //구현 필요
    }

    @FXML
    private void handleDeleteButton() {
        //구현 필요
    }
}
