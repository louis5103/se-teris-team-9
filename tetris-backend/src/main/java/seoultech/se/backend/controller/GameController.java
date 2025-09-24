package seoultech.se.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import seoultech.se.backend.service.GameService;

/**
 * 🎮 기본 REST API 컨트롤러 (Spring DI 통합)
 * 
 * Spring Boot REST API와 서비스 레이어 통합
 * 팀에서 필요한 API 엔드포인트를 구현하세요
 */
@RestController
@RequestMapping("/api")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @GetMapping("/status")
    public String getStatus() {
        return gameService.getStatus();
    }
    
    // TODO: 팀에서 필요한 API 엔드포인트들을 구현하세요
}
