package seoultech.se.core.event;

import lombok.Getter;
import seoultech.se.core.model.Tetromino;

/**
 * 테트로미노가 보드에 고정되었을 때 발생하는 이벤트
 * 
 * 이 이벤트는 블록이 더 이상 움직일 수 없어서 보드에 영구적으로 합쳐질 때 발생합니다.
 * 게임의 중요한 전환점이죠. 블록이 고정되면 여러 일들이 연쇄적으로 일어납니다.
 * 
 * 이 이벤트가 발생한 후 보통 다음과 같은 일들이 순서대로 일어납니다:
 * 1. 라인 클리어 체크 (LineClearedEvent 발생 가능)
 * 2. 점수 계산 (ScoreAddedEvent 발생 가능)
 * 3. 콤보/B2B 체크 (ComboEvent, BackToBackEvent 발생 가능)
 * 4. 게임 오버 체크 (GameOverEvent 발생 가능)
 * 5. 새 블록 생성 (TetrominoSpawnedEvent 발생)
 * 
 * UI 관점에서 이 이벤트는:
 * - 블록이 더 이상 조작 불가능함을 표시
 * - "착지" 사운드 재생
 * - 블록 고정 파티클 이펙트 표시
 * 등의 피드백을 주는 데 사용됩니다.
 * 
 * 멀티플레이어에서는 이 이벤트가 매우 중요합니다.
 * 한 플레이어가 블록을 고정하면, 그 정보가 다른 플레이어들에게도 전송되어야 하니까요.
 * 
 * @see TetrominoSpawnedEvent 블록 고정 후 항상 새 블록이 생성됩니다
 * @see LineClearedEvent 블록 고정 후 라인이 지워질 수 있습니다
 * @see GameOverEvent 블록 고정이 게임 오버를 유발할 수 있습니다
 */
@Getter
public class TetrominoLockedEvent implements GameEvent {
    /**
     * 고정된 테트로미노
     * 
     * 어떤 모양의 블록이 어떤 회전 상태로 고정되었는지 알려줍니다.
     * UI에서 특정 블록 타입에 따라 다른 이펙트를 주고 싶을 때 사용할 수 있습니다.
     * 예를 들어 I 블록이 고정되면 특별한 사운드를 재생할 수도 있겠죠.
     */
    private final Tetromino tetromino;
    
    /**
     * 블록이 고정된 X 위치
     * 
     * 보드에서의 절대 좌표입니다. 이것은 블록의 피봇 기준이 아닌,
     * 보드의 왼쪽 끝을 0으로 하는 좌표입니다.
     */
    private final int x;
    
    /**
     * 블록이 고정된 Y 위치
     * 
     * 보드에서의 절대 좌표입니다. 이것은 블록의 피봇 기준이 아닌,
     * 보드의 맨 위를 0으로 하는 좌표입니다.
     */
    private final int y;
    
    /**
     * 이벤트가 발생한 시각 (밀리초)
     */
    private final long timestamp;
    
    /**
     * TetrominoLockedEvent를 생성합니다
     * 
     * @param tetromino 고정된 테트로미노
     * @param x 고정된 X 위치
     * @param y 고정된 Y 위치
     */
    public TetrominoLockedEvent(Tetromino tetromino, int x, int y) {
        this.tetromino = tetromino;
        this.x = x;
        this.y = y;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.TETROMINO_LOCKED;
    }
    
    @Override
    public String getDescription() {
        return String.format("Tetromino locked: %s at (%d, %d)", 
            tetromino.getType(), x, y);
    }
    
    @Override
    public String toString() {
        return String.format("TetrominoLockedEvent{tetromino=%s, x=%d, y=%d, timestamp=%d}", 
            tetromino.getType(), x, y, timestamp);
    }
}
