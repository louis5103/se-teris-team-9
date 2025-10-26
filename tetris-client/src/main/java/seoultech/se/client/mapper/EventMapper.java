package seoultech.se.client.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import seoultech.se.core.GameState;
import seoultech.se.core.event.BackToBackEvent;
import seoultech.se.core.event.ComboBreakEvent;
import seoultech.se.core.event.ComboEvent;
import seoultech.se.core.event.GameEvent;
import seoultech.se.core.event.GameOverEvent;
import seoultech.se.core.event.GameStateChangedEvent;
import seoultech.se.core.event.LineClearedEvent;
import seoultech.se.core.event.ScoreAddedEvent;
import seoultech.se.core.event.TetrominoLockedEvent;
import seoultech.se.core.event.TetrominoMovedEvent;
import seoultech.se.core.event.TetrominoSpawnedEvent;
import seoultech.se.core.result.LineClearResult;
import seoultech.se.core.result.LockResult;

/**
 * Result 객체를 GameEvent 리스트로 변환하는 매퍼 클래스
 * 
 * 이 클래스는 BoardController의 복잡도를 줄이기 위해 도입되었습니다.
 * Result → Event 변환 로직을 한 곳에 모아서 관리합니다.
 * 
 * 설계 원칙:
 * - Single Responsibility: Event 변환에만 집중
 * - Stateless: 모든 메서드가 static (상태 없음)
 * - Pure Functions: 같은 입력에 대해 항상 같은 출력
 * 
 * 사용 예시:
 * <pre>
 * LockResult lockResult = GameEngine.lockTetromino(gameState);
 * List&lt;GameEvent&gt; events = EventMapper.fromLockResult(
 *     lockResult, 
 *     gameState, 
 *     gameStartTime,
 *     this::spawnNewTetromino  // 새 블록 생성 콜백
 * );
 * </pre>
 * 
 * 왜 정적 메서드를 사용하나요?
 * EventMapper는 상태를 가지지 않고 순수한 변환만 수행하므로,
 * 인스턴스를 생성할 필요가 없습니다. 정적 메서드가 더 효율적입니다.
 * 
 * @author Tetris Team
 * @since 2024-10
 */
@Component
public class EventMapper {

    /**
     * LockResult를 GameEvent 리스트로 변환합니다
     * 
     * 이 메서드는 테트로미노 고정 후 발생하는 모든 이벤트를 생성합니다:
     * 1. TetrominoLockedEvent - 블록이 고정됨
     * 2. GameOverEvent - 게임 오버 (해당하는 경우)
     * 3. LineClearedEvent - 라인이 지워짐 (해당하는 경우)
     * 4. ScoreAddedEvent - 점수 획득 (해당하는 경우)
     * 5. ComboEvent / ComboBreakEvent - 콤보 관련
     * 6. BackToBackEvent - B2B 관련
     * 7. GameStateChangedEvent - 상태 변경
     * 8. TetrominoSpawnedEvent - 새 블록 생성 (게임 오버가 아닌 경우)
     * 9. TetrominoMovedEvent - 새 블록 위치
     * 
     * @param result 고정 결과
     * @param gameState 현재 게임 상태 (새 블록 생성 후)
     * @param gameStartTime 게임 시작 시간 (게임 오버 시 플레이 타임 계산용)
     * @return 발생한 이벤트들의 리스트
     */
    public static List<GameEvent> fromLockResult(
            LockResult result,
            GameState gameState,
            long gameStartTime
    ) {
        List<GameEvent> events = new ArrayList<>();

        // 1. 블록 고정 Event - LockResult에서 고정된 블록 정보 사용
        events.add(new TetrominoLockedEvent(
            result.getLockedTetromino(),  // ✅ 수정됨: 실제 고정된 블록
            result.getLockedX(),           // ✅ 수정됨: 고정된 X 위치
            result.getLockedY()            // ✅ 수정됨: 고정된 Y 위치
        ));

        // 2. 게임 오버 체크
        if (result.isGameOver()) {
            events.addAll(createGameOverEvents(result, gameState, gameStartTime));
            return events; // 게임 오버면 여기서 종료
        }

        // 3. 라인 클리어 처리
        LineClearResult clearResult = result.getLineClearResult();
        if (clearResult.getLinesCleared() > 0) {
            events.addAll(createLineClearEvents(clearResult, gameState));
        } else {
            events.addAll(createNoLineClearEvents(gameState));
        }
        
        // 4. 레벨업 체크
        if (result.isLeveledUp()) {
            events.add(new seoultech.se.core.event.LevelUpEvent(result.getNewLevel()));
        }

        // 5. GameState 변경 Event
        events.add(new GameStateChangedEvent(gameState));

        // 6. 새 블록 관련 이벤트는 BoardController에서 별도 생성
        // createTetrominoSpawnEvents() 메서드 사용

        return events;
    }

    /**
     * 게임 오버 관련 이벤트들을 생성합니다
     */
    private static List<GameEvent> createGameOverEvents(
            LockResult result,
            GameState gameState,
            long gameStartTime
    ) {
        List<GameEvent> events = new ArrayList<>();

        long playTimeMillis = System.currentTimeMillis() - gameStartTime;
        events.add(new GameOverEvent(
            result.getGameOverReason(),
            gameState.getScore(),
            gameState.getLevel(),
            gameState.getLinesCleared(),
            playTimeMillis
        ));

        events.add(new GameStateChangedEvent(gameState));

        return events;
    }

    /**
     * 라인 클리어 관련 이벤트들을 생성합니다
     */
    private static List<GameEvent> createLineClearEvents(
            LineClearResult clearResult,
            GameState gameState
    ) {
        List<GameEvent> events = new ArrayList<>();

        // LineClearedEvent
        events.add(new LineClearedEvent(
            clearResult.getLinesCleared(),
            clearResult.getClearedRows(),
            clearResult.isTSpin(),
            clearResult.isTSpinMini(),
            clearResult.isPerfectClear()
        ));

        // ScoreAddedEvent
        events.add(new ScoreAddedEvent(
            clearResult.getScoreEarned(),
            getScoreReason(clearResult)
        ));

        // Combo Event
        if (gameState.getComboCount() > 0) {
            events.add(new ComboEvent(gameState.getComboCount()));
        }

        // Back-to-Back Event
        if (gameState.getBackToBackCount() > 0) {
            events.add(new BackToBackEvent(gameState.getBackToBackCount()));
        }

        return events;
    }

    /**
     * 라인을 지우지 못했을 때의 이벤트들을 생성합니다
     */
    private static List<GameEvent> createNoLineClearEvents(GameState gameState) {
        List<GameEvent> events = new ArrayList<>();

        // 라인을 지우지 못했으면 콤보 종료
        if (gameState.getComboCount() > 0) {
            events.add(new ComboBreakEvent(gameState.getComboCount()));
        }

        return events;
    }

    /**
     * 새 블록 생성 관련 이벤트들을 생성합니다
     * 
     * 이 메서드는 BoardController에서 spawnNewTetromino() 호출 후 사용됩니다.
     */
    public static List<GameEvent> createTetrominoSpawnEvents(GameState gameState) {
        List<GameEvent> events = new ArrayList<>();

        events.add(new TetrominoSpawnedEvent(
            gameState.getCurrentTetromino(),
            gameState.getCurrentX(),
            gameState.getCurrentY()
        ));

        events.add(new TetrominoMovedEvent(
            gameState.getCurrentX(),
            gameState.getCurrentY(),
            gameState.getCurrentTetromino()
        ));

        return events;
    }

    /**
     * LineClearResult를 기반으로 점수 획득 이유를 문자열로 반환합니다
     * 
     * 반환 가능한 값들:
     * - "PERFECT_CLEAR" - 보드의 모든 블록 제거
     * - "T-SPIN_MINI_SINGLE", "T-SPIN_MINI_DOUBLE" - T-Spin Mini
     * - "T-SPIN_SINGLE", "T-SPIN_DOUBLE", "T-SPIN_TRIPLE" - T-Spin
     * - "SINGLE", "DOUBLE", "TRIPLE", "TETRIS" - 일반 라인 클리어
     * 
     * @param result 라인 클리어 결과
     * @return 점수 이유 문자열
     */
    public static String getScoreReason(LineClearResult result) {
        if (result.isPerfectClear()) {
            return "PERFECT_CLEAR";
        }
        
        if (result.isTSpin()) {
            if (result.isTSpinMini()) {
                return "T-SPIN_MINI_" + lineCountToName(result.getLinesCleared());
            } else {
                return "T-SPIN_" + lineCountToName(result.getLinesCleared());
            }
        }
        
        return lineCountToName(result.getLinesCleared());
    }

    /**
     * 라인 수를 이름으로 변환합니다
     * 
     * @param lines 지워진 라인 수
     * @return "SINGLE", "DOUBLE", "TRIPLE", "TETRIS", 또는 "UNKNOWN"
     */
    private static String lineCountToName(int lines) {
        switch (lines) {
            case 1: return "SINGLE";
            case 2: return "DOUBLE";
            case 3: return "TRIPLE";
            case 4: return "TETRIS";
            default: return "UNKNOWN";
        }
    }
}
