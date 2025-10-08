package seoultech.se.core.event;

import lombok.Getter;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;

/**
 * 테트로미노가 성공적으로 회전했을 때 발생하는 이벤트
 * 
 * 이 이벤트는 블록이 회전에 성공했을 때 발생합니다. 여기서 "성공"이란
 * SRS(Super Rotation System) Wall Kick을 포함하여 최종적으로 회전이 완료되었다는 의미입니다.
 * 
 * SRS Wall Kick 시스템:
 * 테트리스에서 회전은 단순하지 않습니다. 벽이나 다른 블록에 막혀서 회전이 불가능해 보일 때도,
 * 블록을 살짝 이동시켜서 회전을 성공시키려고 시도합니다. 이것이 Wall Kick입니다.
 * 
 * 5가지 위치를 순서대로 시도합니다:
 * - Test 0: 기본 위치 (그냥 회전)
 * - Test 1~4: 다양한 방향으로 이동하면서 회전
 * 
 * 이 중 하나라도 성공하면 회전이 완료되고, 이 이벤트가 발생합니다.
 * kickIndex는 어떤 테스트가 성공했는지 알려줍니다.
 * 
 * kickIndex의 의미:
 * - 0: 기본 위치에서 바로 회전 성공 (Wall Kick 불필요)
 * - 1~4: Wall Kick을 사용하여 회전 성공 (어떤 오프셋을 사용했는지)
 * 
 * UI 관점에서 이 이벤트는:
 * - 블록의 회전 애니메이션 재생
 * - "회전" 사운드 효과
 * - kickIndex가 0이 아니면 "특수 회전" 효과 (선택적)
 * - T-Spin 감지의 기초 자료 (T 블록의 회전 + kickIndex)
 * 등에 사용됩니다.
 * 
 * 만약 회전이 실패하면 이 이벤트는 발생하지 않고,
 * 대신 TetrominoRotationFailedEvent가 발생합니다.
 * 
 * @see TetrominoRotationFailedEvent 회전 실패 시 발생하는 이벤트
 * @see TetrominoMovedEvent 회전 성공 시 위치도 함께 변경될 수 있습니다
 */
@Getter
public class TetrominoRotatedEvent implements GameEvent {
    /**
     * 회전한 테트로미노
     * 
     * 회전이 완료된 후의 새로운 상태입니다.
     * 새로운 RotationState를 가지고 있습니다.
     */
    private final Tetromino tetromino;
    
    /**
     * 회전 방향
     * 
     * CLOCKWISE (시계 방향) 또는 COUNTER_CLOCKWISE (반시계 방향) 중 하나입니다.
     * 대부분의 테트리스에서는 UP 키가 시계 방향, Z 키가 반시계 방향입니다.
     */
    private final RotationDirection direction;
    
    /**
     * Wall Kick 인덱스
     * 
     * 0~4의 값을 가지며, 어떤 Wall Kick 테스트가 성공했는지 나타냅니다.
     * - 0: Wall Kick 없이 바로 회전 성공
     * - 1~4: 해당 번호의 Wall Kick 오프셋을 사용하여 회전 성공
     * 
     * 이 정보는 주로 다음 용도로 사용됩니다:
     * - T-Spin 감지 (특정 kickIndex는 T-Spin을 의미할 수 있음)
     * - 디버깅 (어떤 Wall Kick이 자주 사용되는지 분석)
     * - 고급 플레이어를 위한 정보 표시
     */
    private final int kickIndex;
    
    /**
     * 회전 후 X 위치
     * 
     * Wall Kick으로 인해 회전 전과 다를 수 있습니다.
     */
    private final int newX;
    
    /**
     * 회전 후 Y 위치
     * 
     * Wall Kick으로 인해 회전 전과 다를 수 있습니다.
     */
    private final int newY;
    
    /**
     * 이벤트가 발생한 시각 (밀리초)
     */
    private final long timestamp;
    
    /**
     * TetrominoRotatedEvent를 생성합니다
     * 
     * @param tetromino 회전된 테트로미노
     * @param direction 회전 방향
     * @param kickIndex Wall Kick 인덱스 (0~4)
     * @param newX 회전 후 X 위치
     * @param newY 회전 후 Y 위치
     */
    public TetrominoRotatedEvent(Tetromino tetromino, RotationDirection direction, 
                                 int kickIndex, int newX, int newY) {
        this.tetromino = tetromino;
        this.direction = direction;
        this.kickIndex = kickIndex;
        this.newX = newX;
        this.newY = newY;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.TETROMINO_ROTATED;
    }
    
    @Override
    public String getDescription() {
        String kickInfo = kickIndex == 0 ? "no kick" : "kick " + kickIndex;
        return String.format("Tetromino rotated: %s %s at (%d, %d) [%s]", 
            tetromino.getType(), direction, newX, newY, kickInfo);
    }
    
    @Override
    public String toString() {
        return String.format("TetrominoRotatedEvent{tetromino=%s, direction=%s, kickIndex=%d, newX=%d, newY=%d, timestamp=%d}", 
            tetromino.getType(), direction, kickIndex, newX, newY, timestamp);
    }
}
