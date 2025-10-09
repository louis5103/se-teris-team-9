package seoultech.se.core.event;

/**
 * 게임 이벤트의 기본 인터페이스
 * 
 * Event는 게임에서 "무슨 일이 일어났다"는 사실을 표현합니다.
 * Command가 의도라면, Event는 결과입니다.
 * 
 * Event와 Result의 차이:
 * 
 * GameEngine은 Result 객체를 반환합니다 (MoveResult, RotationResult, LockResult 등).
 * 이것은 엔진의 언어입니다. "새로운 GameState가 이렇고, 성공 여부가 이렇다"는 기술적 정보죠.
 * 
 * 하지만 UI나 네트워크 계층은 이런 기술적 세부사항을 모두 알 필요가 없습니다.
 * 그들은 "테트로미노가 이동했다", "라인이 지워졌다" 같은 비즈니스 이벤트만 알면 됩니다.
 * 
 * 따라서 SessionManager나 GameService는 Result를 Event로 변환합니다:
 * - MoveResult (성공) → TetrominoMovedEvent
 * - RotationResult (성공) → TetrominoRotatedEvent + TetrominoMovedEvent
 * - LockResult → TetrominoLockedEvent, LineClearedEvent, ScoreAddedEvent, TetrominoSpawnedEvent...
 * 
 * 하나의 Result가 여러 Event로 분해될 수 있습니다.
 * 예를 들어 LockResult는 블록이 고정되고, 라인이 지워지고, 점수가 오르고,
 * 새 블록이 생성되는 일련의 이벤트들로 변환됩니다.
 * 
 * Event의 역할:
 * 
 * 1. UI 업데이트:
 *    BoardObserver의 메서드들이 Event를 받아서 화면을 갱신합니다.
 *    onTetrominoMoved(), onLineCleared() 같은 메서드들이 호출되죠.
 * 
 * 2. 네트워크 전송:
 *    멀티플레이어에서는 Event를 JSON으로 직렬화하여 다른 클라이언트에게 전송합니다.
 *    한 플레이어의 행동이 다른 플레이어에게 영향을 미치니까요.
 * 
 * 3. 로깅 및 분석:
 *    Event를 기록하면 게임 진행 과정을 완전히 재구성할 수 있습니다.
 *    리플레이 기능이나 게임 분석에 사용됩니다.
 * 
 * 4. 사운드 및 이펙트:
 *    Event를 받아서 적절한 사운드나 파티클 이펙트를 재생할 수 있습니다.
 *    LineClearedEvent면 "짠!" 소리와 번쩍이는 효과를 줄 수 있죠.
 * 
 * Event의 설계 원칙:
 * 
 * 1. 불변성: Event는 한 번 만들어지면 변경되지 않습니다.
 * 2. 완전성: Event는 필요한 모든 정보를 포함해야 합니다.
 * 3. 직렬화 가능: JSON으로 변환할 수 있어야 합니다 (네트워크 전송용).
 * 4. 타입 안전성: EventType enum으로 타입을 구분합니다.
 */
public interface GameEvent {
    /**
     * 이 Event의 타입을 반환합니다
     * 
     * EventType은 Event를 구분하는 식별자입니다.
     * JSON 직렬화/역직렬화시 어떤 Event 클래스로 변환할지 결정하는 데 사용됩니다.
     * 
     * @return Event의 타입
     */
    EventType getType();
    
    /**
     * Event가 발생한 시각을 반환합니다 (밀리초)
     * 
     * 이 타임스탬프는 여러 용도로 사용됩니다:
     * - 멀티플레이어에서 이벤트 순서 보장
     * - 리플레이 시스템에서 정확한 타이밍 재현
     * - 게임 분석에서 시간대별 패턴 파악
     * 
     * @return Event 발생 시각 (System.currentTimeMillis())
     */
    long getTimestamp();
    
    /**
     * Event에 대한 설명을 반환합니다 (디버깅용)
     * 
     * 로그나 디버그 콘솔에서 읽기 쉬운 형태로 Event를 표시할 때 사용됩니다.
     * 예: "Tetromino moved to (5, 10)", "Line cleared: 4 lines (TETRIS)"
     * 
     * @return Event의 설명 문자열
     */
    default String getDescription() {
        return getType().toString();
    }
}
