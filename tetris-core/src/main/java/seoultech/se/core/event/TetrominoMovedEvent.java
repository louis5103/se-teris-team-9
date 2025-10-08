package seoultech.se.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seoultech.se.core.model.Tetromino;

/**
 * 테트로미노 이동 이벤트
 * 
 * 이 Event는 테트로미노가 새로운 위치로 이동했을 때 발생합니다.
 * 왼쪽, 오른쪽, 아래로의 모든 이동을 포함합니다.
 * 
 * 왜 이 Event가 필요한가?
 * 
 * UI는 블록의 현재 위치를 알아야 화면에 그릴 수 있습니다.
 * 하지만 UI가 직접 GameState를 계속 폴링(polling)하는 것은 비효율적입니다.
 * 대신 "블록이 이동했다"는 Event를 받으면, 그때만 화면을 업데이트하면 됩니다.
 * 
 * 이것이 Observer 패턴의 핵심입니다. Push 방식으로 변경을 알리는 거죠.
 * 
 * 이 Event에 포함된 정보:
 * 
 * 1. newX, newY: 블록의 새로운 좌표
 *    이것은 블록의 pivot(중심점) 위치입니다.
 *    실제 블록의 각 셀 위치는 이 좌표와 shape를 조합하여 계산합니다.
 * 
 * 2. tetromino: 이동한 테트로미노 객체
 *    블록의 타입(I, O, T, S, Z, L, J), 색상, 현재 shape, 회전 상태를 포함합니다.
 *    UI는 이 정보로 어떤 색으로 어떤 모양을 그릴지 결정합니다.
 * 
 * 3. timestamp: Event 발생 시각
 *    멀티플레이어에서 여러 Event의 순서를 보장하거나,
 *    리플레이 시스템에서 정확한 타이밍을 재현하는 데 사용됩니다.
 * 
 * UI에서의 처리:
 * 
 * GameController는 BoardObserver로서 이 Event를 받습니다.
 * onTetrominoMoved() 메서드가 호출되면:
 * 
 * 1. 이전 위치의 블록을 지웁니다 (전체 보드를 다시 그림)
 * 2. 새 위치에 블록을 그립니다
 * 3. 필요하면 Ghost Piece(블록이 떨어질 위치의 반투명 표시)도 업데이트합니다
 * 
 * 멀티플레이어에서의 사용:
 * 
 * 한 플레이어의 블록 이동은 다른 플레이어에게 직접적인 영향을 주지 않습니다.
 * 따라서 이 Event는 보통 로컬 클라이언트에서만 처리됩니다.
 * 
 * 하지만 관전 모드나 협동 모드에서는 다른 플레이어의 이동도 볼 수 있어야 하므로,
 * 이 Event를 네트워크로 전송하여 모든 클라이언트가 같은 화면을 보게 할 수 있습니다.
 * 
 * 최적화 고려사항:
 * 
 * 이 Event는 매우 자주 발생합니다. 블록이 한 칸 내려올 때마다, 
 * 사용자가 좌우로 이동할 때마다, 회전할 때마다 발생하니까요.
 * 
 * 따라서 성능이 중요합니다. Event 객체를 매번 새로 만드는 대신,
 * 객체 풀(Object Pool)을 사용하여 재활용할 수도 있습니다.
 * 
 * 하지만 현대 JVM의 가비지 컬렉터는 매우 효율적이므로,
 * 일반적인 경우 이런 최적화는 필요 없습니다. Premature optimization을 피하세요.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor  // JSON 역직렬화를 위해 필요
public class TetrominoMovedEvent implements GameEvent {
    /**
     * 테트로미노의 새로운 X 좌표 (pivot 기준)
     */
    private int newX;
    
    /**
     * 테트로미노의 새로운 Y 좌표 (pivot 기준)
     */
    private int newY;
    
    /**
     * 이동한 테트로미노
     * 타입, 색상, shape, 회전 상태를 포함합니다
     */
    private Tetromino tetromino;
    
    /**
     * Event 발생 시각 (밀리초)
     */
    private long timestamp;
    
    /**
     * 편의 생성자: 현재 시각을 자동으로 설정
     * 
     * 대부분의 경우 Event 생성 시점이 곧 발생 시점이므로,
     * 이 생성자를 사용하면 편리합니다.
     */
    public TetrominoMovedEvent(int newX, int newY, Tetromino tetromino) {
        this.newX = newX;
        this.newY = newY;
        this.tetromino = tetromino;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.TETROMINO_MOVED;
    }
    
    @Override
    public String getDescription() {
        return String.format("Tetromino %s moved to (%d, %d)", 
                           tetromino.getType(), newX, newY);
    }
    
    @Override
    public String toString() {
        return String.format("TetrominoMovedEvent{x=%d, y=%d, type=%s, timestamp=%d}",
                           newX, newY, tetromino.getType(), timestamp);
    }
}
