package seoultech.se.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import seoultech.se.client.service.NavigationService;

@Component
public class KeySettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;
    
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private Button downButton;
    @FXML
    private Button floorButton;
    @FXML
    private Button rotateButton;
    @FXML
    private Button backButton;

    @FXML
    private void handleLeftButton() {
     //구현 필요
    }
    @FXML
    private void handleRightButton() {
     //구현 필요
    }
    @FXML
    private void handleDownButton() {
     //구현 필요
    }
    @FXML
    private void handleFloorButton() {
     //구현 필요
    }
    @FXML
    private void handleRotateButton() {
     //구현 필요
    }
    @FXML
    private void handleBackButton(ActionEvent event) throws Exception {
        navigationService.navigateTo("/view/setting-view.fxml");
    }
}
