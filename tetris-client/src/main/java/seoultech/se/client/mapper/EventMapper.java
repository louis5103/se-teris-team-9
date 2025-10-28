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

/**
 * GameState를 GameEvent 리스트로 변환하는 매퍼 클래스
 * 
 * Phase 2: Result 객체 제거 - GameState만으로 모든 정보 전달
 * 
 * 이 클래스는 BoardController의 복잡도를 줄이기 위해 도입되었습니다.
 * GameState → Event 변환 로직을 한 곳에 모아서 관리합니다.
 * 
 * 설계 원칙:
 * - Single Responsibility: Event 변환에만 집중
 * - Stateless: 모든 메서드가 static (상태 없음)
 * - Pure Functions: 같은 입력에 대해 항상 같은 출력
 * 
 * 사용 예시:
 * <pre>
 * GameState newState = GameEngine.lockTetromino(gameState);
 * List&lt;GameEvent&gt; events = EventMapper.fromGameState(
 *     newState, 
 *     gameStartTime
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
     * Phase 2: GameState를 GameEvent 리스트로 변환합니다
     * 
     * 이 메서드는 테트로미노 고정 후 발생하는 모든 이벤트를 생성합니다:
     * 1. TetrominoLockedEvent - 블록이 고정됨
     * 2. GameOverEvent - 게임 오버 (해당하는 경우)
     * 3. LineClearedEvent - 라인이 지워짐 (해당하는 경우)
     * 4. ScoreAddedEvent - 점수 획득 (해당하는 경우)
     * 5. ComboEvent / ComboBreakEvent - 콤보 관련
     * 6. BackToBackEvent - B2B 관련
     * 7. GameStateChangedEvent - 상태 변경
     * 8. LevelUpEvent - 레벨업 (해당하는 경우)
     * 
     * GameState의 Lock 메타데이터를 사용하여 이벤트를 생성합니다:
     * - lastLockedTetromino, lastLockedX, lastLockedY
     * - lastLinesCleared, lastClearedRows, lastScoreEarned
     * - lastIsPerfectClear, lastLeveledUp
     * 
     * @param gameState Lock이 완료된 게임 상태 (메타데이터 포함)
     * @param gameStartTime 게임 시작 시간 (게임 오버 시 플레이 타임 계산용)
     * @return 발생한 이벤트들의 리스트
     */
    public static List<GameEvent> fromGameState(
            GameState gameState,
            long gameStartTime
    ) {
        List<GameEvent> events = new ArrayList<>();

        // 1. 블록 고정 Event - GameState의 메타데이터 사용
        events.add(new TetrominoLockedEvent(
            gameState.getLastLockedTetromino(),
            gameState.getLastLockedX(),
            gameState.getLastLockedY()
        ));

        // 2. 게임 오버 체크
        if (gameState.isGameOver()) {
            events.addAll(createGameOverEvents(gameState, gameStartTime));
            return events; // 게임 오버면 여기서 종료
        }

        // 3. 라인 클리어 처리
        if (gameState.getLastLinesCleared() > 0) {
            events.addAll(createLineClearEvents(gameState));
        } else {
            events.addAll(createNoLineClearEvents(gameState));
        }
        
        // 4. 레벨업 체크
        if (gameState.isLastLeveledUp()) {
            events.add(new seoultech.se.core.event.LevelUpEvent(gameState.getLevel()));
        }

        // 5. GameState 변경 Event
        events.add(new GameStateChangedEvent(gameState));

        // 6. 새 블록 관련 이벤트는 BoardController에서 별도 생성
        // createTetrominoSpawnEvents() 메서드 사용

        return events;
    }

    /**
     * 게임 오버 관련 이벤트들을 생성합니다
     * 
     * Phase 2: GameState에서 직접 정보를 읽습니다
     */
    private static List<GameEvent> createGameOverEvents(
            GameState gameState,
            long gameStartTime
    ) {
        List<GameEvent> events = new ArrayList<>();

        long playTimeMillis = System.currentTimeMillis() - gameStartTime;
        events.add(new GameOverEvent(
            gameState.getGameOverReason(),
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
     * 
     * Phase 2: GameState의 메타데이터를 사용합니다
     */
    private static List<GameEvent> createLineClearEvents(GameState gameState) {
        List<GameEvent> events = new ArrayList<>();

        // LineClearedEvent
        events.add(new LineClearedEvent(
            gameState.getLastLinesCleared(),
            gameState.getLastClearedRows(),
            gameState.isLastLockWasTSpin(),
            gameState.isLastLockWasTSpinMini(),
            gameState.isLastIsPerfectClear()
        ));

        // ScoreAddedEvent
        events.add(new ScoreAddedEvent(
            gameState.getLastScoreEarned(),
            getScoreReason(gameState)
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
     * GameState를 기반으로 점수 획득 이유를 문자열로 반환합니다
     * 
     * Phase 2: LineClearResult 대신 GameState의 메타데이터 사용
     * 
     * 반환 가능한 값들:
     * - "PERFECT_CLEAR" - 보드의 모든 블록 제거
     * - "T-SPIN_MINI_SINGLE", "T-SPIN_MINI_DOUBLE" - T-Spin Mini
     * - "T-SPIN_SINGLE", "T-SPIN_DOUBLE", "T-SPIN_TRIPLE" - T-Spin
     * - "SINGLE", "DOUBLE", "TRIPLE", "TETRIS" - 일반 라인 클리어
     * 
     * @param gameState 게임 상태 (Lock 메타데이터 포함)
     * @return 점수 이유 문자열
     */
    public static String getScoreReason(GameState gameState) {
        if (gameState.isLastIsPerfectClear()) {
            return "PERFECT_CLEAR";
        }
        
        if (gameState.isLastLockWasTSpin()) {
            if (gameState.isLastLockWasTSpinMini()) {
                return "T-SPIN_MINI_" + lineCountToName(gameState.getLastLinesCleared());
            } else {
                return "T-SPIN_" + lineCountToName(gameState.getLastLinesCleared());
            }
        }
        
        return lineCountToName(gameState.getLastLinesCleared());
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
