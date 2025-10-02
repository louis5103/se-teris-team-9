package seoultech.se.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import seoultech.se.backend.service.GameService;

/**
 * 🎮 JavaFX 컨트롤러 (Spring DI 통합)
 * 
 * JavaFX UI와 Spring Boot 서비스를 연결하는 컨트롤러
 * - @Component로 Spring DI 컨테이너에 등록
 * - @Autowired로 서비스 레이어 주입
 * 팀에서 필요한 UI 로직을 구현하세요
 */
@Component
public class MainController {
    
    @Autowired
    private GameService gameService;
    
    /**
     * UI 초기화 메서드 (팀에서 구현)
     */
    public void initialize() {
        System.out.println("✅ MainController initialized with Spring DI");
        System.out.println("📊 Service Status: " + gameService.getStatus());
        // TODO: 팀에서 UI 초기화 로직 구현
    }

    public void handleSettingsButtonAction() {
        System.out.println("⚙️ Settings button clicked");
    }

    public void handleStartButtonAction() {
        System.out.println("▶️ Start button clicked");
    }

    public void handleEndButtonAction() {
        System.out.println("❌ Exit button clicked");
        Platform.exit();
    }
}
