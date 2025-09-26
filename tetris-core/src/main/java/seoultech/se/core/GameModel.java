package seoultech.se.core;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 기본 모델 클래스 - Lombok 적용 예제
 * 
 * @Data: getter, setter, toString, equals, hashCode 자동 생성
 * @Builder: 빌더 패턴 자동 생성
 * @AllArgsConstructor: 모든 필드를 매개변수로 하는 생성자 생성
 * @NoArgsConstructor: 기본 생성자 생성
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameModel {
    
    private String gameId;
    private int score;
    private int level;
    private boolean isGameOver;
    private long startTime;
    
    // Lombok이 다음 메서드들을 자동 생성합니다:
    // - getGameId(), setGameId()
    // - getScore(), setScore()
    // - getLevel(), setLevel()
    // - isGameOver(), setGameOver()
    // - getStartTime(), setStartTime()
    // - toString(), equals(), hashCode()
    // - GameModel.builder()
    
    // TODO: 팀에서 필요한 추가 비즈니스 로직을 구현하세요
}
