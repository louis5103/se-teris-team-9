package seoultech.se.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import seoultech.se.client.service.NavigationService;

@Component
public class OverPopController extends BaseController {
    
    @Autowired
    private NavigationService navigationService;
    
    @FXML
    private Label scoreLabel;
    @FXML
    private HBox nameInputBox;

    @FXML
    private TextField usernameInput;


    /**
     * 점수 설정
     */
    public void setScore(long score) {
        if (scoreLabel != null) {
            scoreLabel.setText(String.valueOf(score));
        }
    }

    /**
     * Main 버튼 핸들러 - 메인 화면으로 이동
     */
    @FXML
    private void handleMainButton(ActionEvent event) {
        try {
            closePopup(event);
            navigationService.navigateTo("/view/main-view.fxml");
        } catch (Exception e) {
            System.err.println("❌ Failed to navigate to main view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Restart 버튼 핸들러 - 게임 재시작
     */
    @FXML
    private void handleRestartButton(ActionEvent event) {
        try {
            closePopup(event);
            navigationService.navigateTo("/view/game-view.fxml");
        } catch (Exception e) {
            System.err.println("❌ Failed to restart game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 팝업 창 닫기
     */
    private void closePopup(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
