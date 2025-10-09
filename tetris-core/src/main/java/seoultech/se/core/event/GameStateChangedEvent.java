package seoultech.se.core.event;

import lombok.Getter;
import seoultech.se.core.GameState;

/**
 * 게임 상태가 변경되었을 때 발생하는 이벤트
 * 
 * 이 이벤트는 조금 특별합니다. 다른 Event들이 "무슨 일이 일어났다"는 구체적인 행동을 알려주는 반면,
 * 이 Event는 "게임의 전반적인 상태가 바뀌었다"는 것을 알려줍니다.
 * 
 * 언제 발생하나요?
 * 
 * 게임 상태가 의미 있게 변경될 때마다 발생합니다. 예를 들어:
 * - 점수가 올랐을 때
 * - 레벨이 변경되었을 때
 * - 지운 라인 수가 증가했을 때
 * - 콤보 카운터가 변경되었을 때
 * - B2B 카운터가 변경되었을 때
 * 
 * 왜 이런 Event가 필요한가요?
 * 
 * 구체적인 Event들(ScoreAddedEvent, LineClearedEvent 등)은 "무슨 일이 일어났는지"를 알려줍니다.
 * 하지만 때로는 "현재 게임이 어떤 상태인지"를 전체적으로 알아야 할 때가 있습니다.
 * 
 * 예를 들어 UI가 처음 로드될 때, 모든 라벨을 업데이트해야 합니다.
 * 점수 라벨, 레벨 라벨, 라인 라벨 등을 모두 설정해야 하죠.
 * 이때 개별 Event를 하나씩 처리하는 것보다, GameState를 통째로 받아서
 * 한 번에 모든 UI를 업데이트하는 것이 더 효율적입니다.
 * 
 * 또 다른 사용 사례는 게임 저장/로드입니다.
 * 게임을 저장하려면 현재 GameState를 직렬화하면 되고,
 * 로드할 때는 저장된 GameState를 복원하고 이 Event를 발행하면 됩니다.
 * 
 * UI 관점에서 이 이벤트는:
 * - 모든 게임 정보 라벨 업데이트 (점수, 레벨, 라인 등)
 * - 게임 속도 조정 (레벨에 따라 블록 낙하 속도 변경)
 * - 통계 화면 갱신
 * - 게임 저장/로드 시 전체 UI 동기화
 * 등에 사용됩니다.
 * 
 * 성능 고려사항:
 * 
 * 이 Event는 GameState 객체 전체를 포함하고 있습니다.
 * GameState는 보드의 모든 셀 정보를 포함하므로, 크기가 상당히 큽니다.
 * 따라서 이 Event를 너무 자주 발행하면 성능 문제가 생길 수 있습니다.
 * 
 * 보통 다음과 같은 경우에만 이 Event를 발행합니다:
 * - 블록이 고정된 후 (한 턴이 완료됨)
 * - 레벨이 올랐을 때
 * - 게임 로드가 완료되었을 때
 * - 게임이 일시정지/재개될 때
 * 
 * 블록이 이동할 때마다 이 Event를 발행하면 안 됩니다!
 * 그런 경우에는 TetrominoMovedEvent를 사용하세요.
 * 
 * @see ScoreAddedEvent 점수 변경에 대한 구체적인 Event
 * @see LineClearedEvent 라인 클리어에 대한 구체적인 Event
 * @see TetrominoMovedEvent 블록 이동에 대한 구체적인 Event
 */
@Getter
public class GameStateChangedEvent implements GameEvent {
    /**
     * 변경된 게임 상태
     * 
     * 현재 게임의 완전한 상태 스냅샷입니다.
     * 이것은 불변 객체이므로, 받은 쪽에서 수정할 수 없습니다.
     * 
     * 포함된 정보:
     * - 보드 상태 (모든 셀의 정보)
     * - 현재 블록 정보
     * - 점수, 레벨, 지운 라인 수
     * - 콤보, B2B 카운터
     * - Next Queue, Hold 정보
     * - 게임 오버 여부
     * 등 게임의 모든 정보가 들어있습니다.
     */
    private final GameState gameState;
    
    /**
     * 이전 점수 (비교용)
     * 
     * 점수가 얼마나 변했는지 알고 싶을 때 사용할 수 있습니다.
     * 예를 들어 "점수가 1000점 증가했습니다!" 같은 메시지를 표시할 수 있죠.
     * 
     * 점수가 변하지 않았다면 이전 점수와 같을 것입니다.
     */
    private final long previousScore;
    
    /**
     * 이전 레벨 (비교용)
     * 
     * 레벨이 올랐는지 확인할 때 사용할 수 있습니다.
     * 레벨업 애니메이션이나 축하 메시지를 표시할 수 있죠.
     */
    private final int previousLevel;
    
    /**
     * 이전 라인 수 (비교용)
     * 
     * 얼마나 많은 라인을 지웠는지 확인할 때 사용할 수 있습니다.
     */
    private final int previousLines;
    
    /**
     * 이벤트가 발생한 시각 (밀리초)
     */
    private final long timestamp;
    
    /**
     * GameStateChangedEvent를 생성합니다
     * 
     * @param gameState 변경된 게임 상태
     * @param previousScore 이전 점수
     * @param previousLevel 이전 레벨
     * @param previousLines 이전 라인 수
     */
    public GameStateChangedEvent(GameState gameState, long previousScore, 
                                 int previousLevel, int previousLines) {
        this.gameState = gameState;
        this.previousScore = previousScore;
        this.previousLevel = previousLevel;
        this.previousLines = previousLines;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 간편한 생성자 (이전 값 없이)
     * 
     * 이전 값을 추적하지 않아도 될 때 사용합니다.
     * 예를 들어 게임 로드 시에는 이전 값이 의미 없으니까요.
     * 
     * @param gameState 변경된 게임 상태
     */
    public GameStateChangedEvent(GameState gameState) {
        this(gameState, gameState.getScore(), gameState.getLevel(), gameState.getLinesCleared());
    }
    
    /**
     * 점수가 증가했는지 확인합니다
     * 
     * @return 점수가 증가했으면 true
     */
    public boolean isScoreIncreased() {
        return gameState.getScore() > previousScore;
    }
    
    /**
     * 레벨이 올랐는지 확인합니다
     * 
     * @return 레벨이 올랐으면 true
     */
    public boolean isLevelUp() {
        return gameState.getLevel() > previousLevel;
    }
    
    /**
     * 지운 라인이 증가했는지 확인합니다
     * 
     * @return 라인이 증가했으면 true
     */
    public boolean isLinesIncreased() {
        return gameState.getLinesCleared() > previousLines;
    }
    
    /**
     * 점수 증가량을 계산합니다
     * 
     * @return 증가한 점수 (음수일 수 없음)
     */
    public long getScoreDelta() {
        return Math.max(0, gameState.getScore() - previousScore);
    }
    
    @Override
    public EventType getType() {
        return EventType.GAME_STATE_CHANGED;
    }
    
    @Override
    public String getDescription() {
        return String.format("Game state changed: Score=%d, Level=%d, Lines=%d", 
            gameState.getScore(), gameState.getLevel(), gameState.getLinesCleared());
    }
    
    @Override
    public String toString() {
        return String.format("GameStateChangedEvent{score=%d(%+d), level=%d, lines=%d, timestamp=%d}", 
            gameState.getScore(), getScoreDelta(), gameState.getLevel(), 
            gameState.getLinesCleared(), timestamp);
    }
}
