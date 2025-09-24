package seoultech.se.backend.service;

import org.springframework.stereotype.Service;

<<<<<<< HEAD
=======
import lombok.extern.slf4j.Slf4j;

>>>>>>> 10c280e (git squash feat/24/branches)
/**
 * 🎯 기본 서비스 클래스 (Spring DI 통합)
 * 
 * Spring Boot의 의존성 주입을 활용한 서비스 레이어
 * 팀에서 필요한 비즈니스 로직을 구현하세요
 */
@Service
<<<<<<< HEAD
public class GameService {
    
    public String getStatus() {
=======
@Slf4j  // Lombok logging annotation
public class GameService {
    
    public String getStatus() {
        log.info("GameService status requested"); // Using Lombok-generated logger
>>>>>>> 10c280e (git squash feat/24/branches)
        return "Service Layer Ready - 팀에서 구현하세요";
    }
    
    // TODO: 팀에서 필요한 서비스 메서드들을 구현하세요
}
