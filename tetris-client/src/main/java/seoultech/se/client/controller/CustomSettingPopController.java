package seoultech.se.client.controller;

import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

@Component
public class CustomSettingPopController extends BaseController {

    @FXML
    private TextField settingNameField; //사용자가 이름 입력하는 field
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @FXML
    private void handleSaveButton() {
        //구현 필요
    }

    @FXML
    private void handleCancelButton() {
        //구현 필요
    }

    @FXML
    private void handleDeleteButton() {
        //구현 필요(input 입력 받았을 때 행동)
    }
}
