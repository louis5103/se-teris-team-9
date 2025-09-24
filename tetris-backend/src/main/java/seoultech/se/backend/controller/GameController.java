package seoultech.se.backend.controller;

<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
=======

>>>>>>> 10c280e (git squash feat/24/branches)
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
=======
import lombok.RequiredArgsConstructor;
>>>>>>> 10c280e (git squash feat/24/branches)
import seoultech.se.backend.service.GameService;

/**
 * 🎮 기본 REST API 컨트롤러 (Spring DI 통합)
 * 
 * Spring Boot REST API와 서비스 레이어 통합
 * 팀에서 필요한 API 엔드포인트를 구현하세요
 */
@RestController
@RequestMapping("/api")
<<<<<<< HEAD
public class GameController {
    
    @Autowired
    private GameService gameService;
=======
@RequiredArgsConstructor  // Lombok: final 필드에 대한 생성자 자동 생성
public class GameController {
    
    // @Autowired 제거 - @RequiredArgsConstructor가 생성자 주입 처리
    private final GameService gameService;
>>>>>>> 10c280e (git squash feat/24/branches)
    
    @GetMapping("/status")
    public String getStatus() {
        return gameService.getStatus();
    }
    
    // TODO: 팀에서 필요한 API 엔드포인트들을 구현하세요
}
