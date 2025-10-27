package seoultech.se.core.mode;

import java.util.List;

import seoultech.se.core.GameState;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.config.GameModeConfig;
import seoultech.se.core.event.GameEvent;
import seoultech.se.core.result.LockResult;

/**
 * 게임 모드 인터페이스 (Strategy Pattern)
 * 
 * 이 인터페이스는 다양한 게임 모드를 구현하기 위한 전략(Strategy)을 정의합니다.
 * BoardController는 이 인터페이스에 의존하며, 런타임에 다른 모드로 교체 가능합니다.
 * 
 * 설계 원칙:
 * 1. Composition over Inheritance
 *    - BoardController가 GameMode를 소유 (has-a)
 *    - 상속이 아닌 위임(delegation) 사용
 * 
 * 2. Open-Closed Principle
 *    - 새 모드 추가 시 기존 코드 수정 불필요
 *    - 확장에는 열려있고 수정에는 닫혀있음
 * 
 * 3. Strategy Pattern
 *    - Context: BoardController
 *    - Strategy: GameMode 인터페이스
 *    - Concrete Strategies: SingleMode, ItemMode, MultiMode
 * 
 * 핵심 확장 포인트:
 * 
 * 1. onLineClear()
 *    - 라인 클리어 후 추가 처리
 *    - SingleMode: 추가 로직 없음
 *    - ItemMode: 아이템 드롭
 *    - MultiMode: 공격 전송
 * 
 * 2. canExecuteCommand()
 *    - Command 실행 전 검증
 *    - 특정 모드에서 특정 명령 제한 가능
 * 
 * 3. cleanup()
 *    - 모드 종료 시 리소스 정리
 *    - 네트워크 연결 해제 등
 */
public interface GameMode {
    
    /**
     * 모드 타입 반환
     * 
     * @return 게임 모드 타입
     */
    GameModeType getType();
    
    /**
     * 모드 설정 반환
     * 
     * @return 게임 모드 설정 객체
     */
    GameModeConfig getConfig();
    
    /**
     * 모드 초기화
     * 
     * 게임 시작 시 호출됩니다.
     * 모드별 초기 설정을 수행합니다.
     * 
     * @param initialState 초기 게임 상태
     */
    void initialize(GameState initialState);
    
    /**
     * ⭐ 라인 클리어 후 추가 처리 (핵심 확장 포인트)
     * 
     * 라인이 클리어된 후 모드별 추가 동작을 정의합니다.
     * 
     * 구현 예시:
     * - SingleMode: 추가 로직 없음 (빈 리스트 반환)
     * - ItemMode: 확률적으로 아이템 드롭 (ItemDroppedEvent 반환)
     * - MultiMode: 공격 계산 및 전송 (AttackSentEvent 반환)
     * 
     * @param result 블록 고정 결과 (라인 클리어 정보 포함)
     * @param state 현재 게임 상태
     * @return 추가로 발생한 이벤트 리스트
     */
    default List<GameEvent> onLineClear(LockResult result, GameState state) {
        return List.of(); // 기본 구현: 추가 이벤트 없음
    }
    
    /**
     * Command 실행 전 검증 (선택적 확장 포인트)
     * 
     * Command가 실행되기 전에 호출됩니다.
     * 특정 모드에서 특정 명령을 제한하고 싶을 때 사용합니다.
     * 
     * 사용 예시:
     * - 하드 모드: 홀드 명령 거부
     * - 멀티플레이: 일시정지 명령 거부
     * - 아이템 모드: 아이템 효과 중 특정 명령 제한
     * 
     * @param command 실행할 명령
     * @param state 현재 게임 상태
     * @return true이면 명령 실행, false이면 무시
     */
    default boolean canExecuteCommand(GameCommand command, GameState state) {
        return true; // 기본 구현: 모든 명령 허용
    }
    
    /**
     * Command 실행 후 추가 처리 (선택적 확장 포인트)
     * 
     * Command 실행 후 모드별 추가 동작을 정의합니다.
     * 
     * @param command 실행된 명령
     * @param state 명령 실행 후 게임 상태
     * @return 추가로 발생한 이벤트 리스트
     */
    default List<GameEvent> afterCommand(GameCommand command, GameState state) {
        return List.of(); // 기본 구현: 추가 이벤트 없음
    }
    
    /**
     * 모드 종료 시 정리
     * 
     * 게임 종료 또는 모드 전환 시 호출됩니다.
     * 리소스 정리, 연결 해제 등을 수행합니다.
     * 
     * 사용 예시:
     * - MultiMode: WebSocket 연결 해제
     * - ItemMode: 타이머 정리
     */
    default void cleanup() {
        // 기본 구현: 아무것도 하지 않음
    }
}
