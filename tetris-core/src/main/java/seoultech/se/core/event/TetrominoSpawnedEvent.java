package seoultech.se.core.event;

import lombok.Getter;
import seoultech.se.core.model.Tetromino;

/**
 * 새로운 테트로미노가 생성되었을 때 발생하는 이벤트
 * 
 * 이 이벤트는 Next Queue에서 다음 블록이 꺼내져서 spawn 위치에 나타날 때 발생합니다.
 * 게임의 연속성을 만드는 핵심 이벤트이죠. 한 블록이 끝나면 바로 다음 블록이 시작됩니다.
 * 
 * 이 이벤트가 발생하는 시점:
 * 1. 게임이 시작될 때 (첫 번째 블록)
 * 2. 블록이 고정된 직후 (다음 블록)
 * 3. Hold를 사용한 직후 (Hold에서 꺼낸 블록 또는 새 블록)
 * 
 * 이 이벤트를 받은 UI는:
 * - 새 블록을 화면에 표시
 * - Next Queue를 한 칸 왼쪽으로 이동
 * - "블록 생성" 사운드 재생
 * - 만약 생성 실패면 게임 오버 처리
 * 등의 작업을 수행합니다.
 * 
 * 7-bag 시스템:
 * 테트리스는 완전한 랜덤이 아니라 7-bag 시스템을 사용합니다.
 * 7가지 블록을 가방에 넣고 섞은 다음, 하나씩 꺼내서 사용하죠.
 * 가방이 비면 다시 7가지를 넣고 섞습니다.
 * 이렇게 하면 같은 블록이 너무 연속으로 나오는 것을 방지할 수 있습니다.
 * 
 * @see TetrominoLockedEvent 블록이 고정되면 새 블록이 생성됩니다
 * @see NextQueueUpdatedEvent 블록이 생성되면 Next Queue가 업데이트됩니다
 */
@Getter
public class TetrominoSpawnedEvent implements GameEvent {
    /**
     * 생성된 테트로미노
     * 
     * 7-bag 시스템에서 꺼낸 새로운 블록입니다.
     * 이 블록은 항상 기본 회전 상태(RotationState.SPAWN)로 생성됩니다.
     */
    private final Tetromino tetromino;
    
    /**
     * 생성된 X 위치
     * 
     * 보통 보드 중앙에 생성됩니다 (boardWidth / 2 - 1).
     * 이것은 표준 테트리스 규칙입니다.
     */
    private final int spawnX;
    
    /**
     * 생성된 Y 위치
     * 
     * 보통 보드 맨 위(Y=0)나 그 위쪽에 생성됩니다.
     * Y < 0인 경우 블록이 보이지 않는 위쪽 영역에 있다는 의미입니다.
     * 이것은 spawn 버퍼 영역이라고 하며, 블록이 자연스럽게 나타나도록 합니다.
     */
    private final int spawnY;
    
    /**
     * 이벤트가 발생한 시각 (밀리초)
     */
    private final long timestamp;
    
    /**
     * TetrominoSpawnedEvent를 생성합니다
     * 
     * @param tetromino 생성된 테트로미노
     * @param spawnX 생성 X 위치
     * @param spawnY 생성 Y 위치
     */
    public TetrominoSpawnedEvent(Tetromino tetromino, int spawnX, int spawnY) {
        this.tetromino = tetromino;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.TETROMINO_SPAWNED;
    }
    
    @Override
    public String getDescription() {
        return String.format("Tetromino spawned: %s at (%d, %d)", 
            tetromino.getType(), spawnX, spawnY);
    }
    
    @Override
    public String toString() {
        return String.format("TetrominoSpawnedEvent{tetromino=%s, spawnX=%d, spawnY=%d, timestamp=%d}", 
            tetromino.getType(), spawnX, spawnY, timestamp);
    }
}
