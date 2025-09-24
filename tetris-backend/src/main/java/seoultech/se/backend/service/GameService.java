package seoultech.se.backend.service;

import org.springframework.stereotype.Service;

/**
 * 🎯 기본 서비스 클래스 (Spring DI 통합)
 * 
 * Spring Boot의 의존성 주입을 활용한 서비스 레이어
 * 팀에서 필요한 비즈니스 로직을 구현하세요
 */
@Service
public class GameService {
    
    public String getStatus() {
        return "Service Layer Ready - 팀에서 구현하세요";
    }
    
    // TODO: 팀에서 필요한 서비스 메서드들을 구현하세요
}
