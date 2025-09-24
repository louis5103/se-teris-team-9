package seoultech.se.core;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * GameModel 테스트 클래스
 * Lombok이 정상적으로 작동하는지 확인
 */
class GameModelTest {

    @Test
    void testLombokDataAnnotation() {
        // Lombok @Data가 생성한 메서드들이 정상 작동하는지 확인
        GameModel game1 = new GameModel();
        game1.setGameId("game-001");
        game1.setScore(1000);
        game1.setLevel(5);
        game1.setGameOver(false);
        game1.setStartTime(System.currentTimeMillis());
        
        assertThat(game1.getGameId()).isEqualTo("game-001");
        assertThat(game1.getScore()).isEqualTo(1000);
        assertThat(game1.getLevel()).isEqualTo(5);
        assertThat(game1.isGameOver()).isFalse();
        assertThat(game1.getStartTime()).isPositive();
    }

    @Test
    void testLombokBuilderPattern() {
        // Lombok @Builder가 정상 작동하는지 확인
        GameModel game = GameModel.builder()
            .gameId("builder-test")
            .score(2500)
            .level(10)
            .isGameOver(true)
            .startTime(1234567890L)
            .build();
        
        assertThat(game.getGameId()).isEqualTo("builder-test");
        assertThat(game.getScore()).isEqualTo(2500);
        assertThat(game.getLevel()).isEqualTo(10);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.getStartTime()).isEqualTo(1234567890L);
    }

    @Test
    void testLombokEqualsAndHashCode() {
        // Lombok이 생성한 equals, hashCode가 정상 작동하는지 확인
        GameModel game1 = GameModel.builder()
            .gameId("test-game")
            .score(100)
            .level(1)
            .build();
            
        GameModel game2 = GameModel.builder()
            .gameId("test-game")
            .score(100)
            .level(1)
            .build();
            
        assertThat(game1).isEqualTo(game2);
        assertThat(game1.hashCode()).isEqualTo(game2.hashCode());
    }

    @Test
    void testLombokToString() {
        // Lombok이 생성한 toString이 정상 작동하는지 확인
        GameModel game = GameModel.builder()
            .gameId("toString-test")
            .score(999)
            .build();
            
        String gameString = game.toString();
        assertThat(gameString).contains("toString-test");
        assertThat(gameString).contains("999");
        assertThat(gameString).contains("GameModel");
    }
}
