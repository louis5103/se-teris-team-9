package seoultech.se.client.controller;

import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

@Component
public class GameController extends BaseController {
    @FXML
    private BorderPane rootPane;

    @FXML
    public void initialize() {
        super.initialize();
    }

    @FXML
    private void handleStartPauseResume() {
        System.out.println("▶️ Start/Pause/Resume button clicked");
    }

    @FXML
    private void handleExit() {
        System.out.println("❌ Exit button clicked");
        Platform.exit();
    }
}
