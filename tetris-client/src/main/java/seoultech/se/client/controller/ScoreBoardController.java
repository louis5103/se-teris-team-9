package seoultech.se.client.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import seoultech.se.client.service.NavigationService;

@Component
public class ScoreBoardController extends BaseController {

    @Autowired
    private NavigationService navigationService;

    @FXML
    private RadioButton normalMode;
    @FXML
    private RadioButton itemMode;

    @FXML
    private void handleBackButton() throws IOException {
       navigationService.navigateTo("/view/main-view.fxml");
    }

    @FXML
    private void handleNormalMode() {
        // Handle normal mode selection
    }

    @FXML
    private void handleItemMode() {
        // Handle item mode selection
    }

}
