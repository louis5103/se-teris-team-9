package seoultech.se.core.event;

/**
 * Event의 종류를 나타내는 열거형
 * 
 * 이 enum은 게임에서 발생할 수 있는 모든 이벤트 타입을 정의합니다.
 * 각 Event 클래스는 이 enum 값 중 하나를 가집니다.
 * 
 * Event 타입은 크게 네 가지 카테고리로 나눌 수 있습니다:
 * 
 * 1. 테트로미노 관련 이벤트:
 *    블록의 이동, 회전, 고정, 생성 등
 * 
 * 2. 게임 진행 이벤트:
 *    라인 클리어, 점수 획득, 레벨업, 게임 오버 등
 * 
 * 3. 게임 상태 이벤트:
 *    Hold 변경, Next Queue 업데이트, 일시정지/재개 등
 * 
 * 4. 멀티플레이어 이벤트:
 *    가비지 라인 공격, 콤보, Back-to-Back 등
 */
public enum EventType {
    // ===== 테트로미노 관련 이벤트 =====
    
    /**
     * 테트로미노가 이동했을 때
     * 왼쪽, 오른쪽, 아래로의 모든 이동을 포함합니다
     */
    TETROMINO_MOVED,
    
    /**
     * 테트로미노가 회전했을 때
     * SRS Wall Kick이 적용되어 회전에 성공한 경우입니다
     */
    TETROMINO_ROTATED,
    
    /**
     * 테트로미노 회전이 실패했을 때
     * 모든 Wall Kick을 시도해도 회전할 수 없는 경우입니다
     */
    TETROMINO_ROTATION_FAILED,
    
    /**
     * 테트로미노가 보드에 고정되었을 때
     * 더 이상 내려갈 수 없어서 블록이 보드에 합쳐진 상태입니다
     */
    TETROMINO_LOCKED,
    
    /**
     * 새로운 테트로미노가 생성되었을 때
     * Next Queue에서 다음 블록이 spawn 위치에 나타난 상태입니다
     */
    TETROMINO_SPAWNED,
    
    // ===== 게임 진행 이벤트 =====
    
    /**
     * 라인이 지워졌을 때
     * 1줄(Single), 2줄(Double), 3줄(Triple), 4줄(Tetris)을 모두 포함합니다
     * T-Spin, Perfect Clear 정보도 함께 전달됩니다
     */
    LINE_CLEARED,
    
    /**
     * 점수가 추가되었을 때
     * 라인 클리어, Hard Drop, Soft Drop 등으로 점수를 얻은 경우입니다
     */
    SCORE_ADDED,
    
    /**
     * 게임 상태가 변경되었을 때
     * 점수, 레벨, 라인 수 등의 통계 정보가 업데이트된 경우입니다
     */
    GAME_STATE_CHANGED,
    
    /**
     * 레벨이 올랐을 때
     * 보통 10줄을 지울 때마다 레벨업합니다
     */
    LEVEL_UP,
    
    /**
     * 게임이 종료되었을 때
     * 블록이 spawn 위치를 벗어나거나 Top Out된 경우입니다
     */
    GAME_OVER,
    
    // ===== 게임 상태 이벤트 =====
    
    /**
     * Hold가 변경되었을 때
     * 현재 블록을 Hold에 저장하거나, Hold에서 블록을 꺼낸 경우입니다
     */
    HOLD_CHANGED,
    
    /**
     * Hold가 실패했을 때
     * 이미 이번 턴에 Hold를 사용했거나, 다른 이유로 Hold가 불가능한 경우입니다
     */
    HOLD_FAILED,
    
    /**
     * Next Queue가 업데이트되었을 때
     * 7-bag 시스템에서 다음에 나올 블록 목록이 변경된 경우입니다
     */
    NEXT_QUEUE_UPDATED,
    
    /**
     * 게임이 일시정지되었을 때
     */
    GAME_PAUSED,
    
    /**
     * 게임이 재개되었을 때
     */
    GAME_RESUMED,
    
    // ===== 콤보 및 특수 이벤트 =====
    
    /**
     * 콤보가 발생했을 때
     * 연속으로 라인을 지우면 콤보 카운터가 올라갑니다
     */
    COMBO,
    
    /**
     * 콤보가 끊겼을 때
     * 라인을 지우지 못하고 블록을 고정하면 콤보가 리셋됩니다
     */
    COMBO_BREAK,
    
    /**
     * Back-to-Back가 발생했을 때
     * Tetris나 T-Spin을 연속으로 하면 B2B 카운터가 올라갑니다
     */
    BACK_TO_BACK,
    
    /**
     * Back-to-Back가 끊겼을 때
     * Difficult 클리어(Tetris, T-Spin)가 아닌 일반 클리어를 하면 B2B가 리셋됩니다
     */
    BACK_TO_BACK_BREAK,
    
    // ===== 멀티플레이어 이벤트 =====
    
    /**
     * 가비지 라인이 추가되었을 때
     * 다른 플레이어의 공격으로 쓰레기 라인이 바닥에서 올라온 경우입니다
     */
    GARBAGE_LINES_ADDED,
    
    /**
     * 가비지 라인이 방어되었을 때
     * 라인을 지워서 들어오는 가비지 라인을 상쇄한 경우입니다
     */
    GARBAGE_LINES_CLEARED,
    
    /**
     * 공격을 보냈을 때
     * 자신의 라인 클리어로 다른 플레이어에게 가비지 라인을 보낸 경우입니다
     */
    ATTACK_SENT,
    
    // ===== Lock Delay 관련 이벤트 =====
    
    /**
     * Lock Delay가 시작되었을 때
     * 블록이 바닥에 닿아서 고정 타이머가 시작된 상태입니다
     */
    LOCK_DELAY_STARTED,
    
    /**
     * Lock Delay가 리셋되었을 때
     * 블록을 이동시키거나 회전시켜서 고정 타이머가 연장된 상태입니다
     */
    LOCK_DELAY_RESET,
    
    // ===== 디버그 이벤트 =====
    
    /**
     * 디버그 정보가 업데이트되었을 때
     * 개발 중에 내부 상태를 확인하기 위한 이벤트입니다
     */
    DEBUG_INFO_UPDATED
}
