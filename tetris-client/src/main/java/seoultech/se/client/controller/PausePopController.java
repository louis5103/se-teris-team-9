package seoultech.se.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import seoultech.se.client.service.NavigationService;

@Component
public class PausePopController extends BaseController {

    @Autowired
    private NavigationService navigationService;
    
    private GameController gameController;

    /**
     * GameController 설정
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Resume 버튼 핸들러 - 게임 재개
     */
    @FXML
    private void handleResume(ActionEvent event) {
        if (gameController != null) {
            gameController.resumeGame();
        }
        closePopup(event);
    }

    /**
     * Quit 버튼 핸들러 - 메인 화면으로 이동
     */
    @FXML
    private void handleQuit(ActionEvent event) {
        try {
            closePopup(event);
            navigationService.navigateTo("/view/main-view.fxml");
        } catch (Exception e) {
            System.err.println("❌ Failed to navigate to main view: " + e.getMessage());
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
