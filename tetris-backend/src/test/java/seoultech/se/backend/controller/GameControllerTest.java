package seoultech.se.backend.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * GameController 테스트 클래스
 * Lombok과 테스트 의존성이 정상적으로 인식되는지 확인
 */
@SpringBootTest
class GameControllerTest {

    @Autowired
    private TetrisAppController gameController;

    @Test
    void contextLoads() {
        // 컨텍스트가 정상적으로 로드되는지 확인
        assertThat(gameController).isNotNull();
    }

    @Test
    void getStatus_shouldReturnServiceStatus() {
        // Lombok @RequiredArgsConstructor가 정상 작동하는지 확인
        String status = gameController.getStatus();
        assertThat(status).isNotNull();
        assertThat(status).contains("Service Layer Ready");
    }
}
