package seoultech.se.client.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import seoultech.se.backend.service.GameService;
import seoultech.se.client.service.NavigationService;

/**
 * 🎮 JavaFX 컨트롤러 (Spring DI 통합)
 * 
 * JavaFX UI와 Spring Boot 서비스를 연결하는 컨트롤러
 * - @Component로 Spring DI 컨테이너에 등록
 * - @Autowired로 서비스 레이어 주입
 * 팀에서 필요한 UI 로직을 구현하세요
 */
@Component
public class MainController extends BaseController {
    
    @Autowired
    private GameService gameService;

    @Autowired
    private NavigationService navigationService;
    
    @FXML
    public void initialize() {
        System.out.println("✅ MainController initialized with Spring DI");
        System.out.println("📊 Service Status: " + gameService.getStatus());
        super.initialize();
    }
    
    public void handleStartButtonAction(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/game-view.fxml");
    }

    public void handleScoreButtonAction() {
        System.out.println("🏆 Score button clicked");
    }
    
    public void handleEndButtonAction() {
        System.out.println("❌ Exit button clicked");
        Platform.exit();
    }

    public void handleSettingsButtonAction(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/setting-view.fxml");
    }
}
