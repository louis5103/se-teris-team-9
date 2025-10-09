package seoultech.se.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import lombok.Getter;
import seoultech.se.core.BoardObserver;
import seoultech.se.core.GameEngine;
import seoultech.se.core.GameState;
import seoultech.se.core.command.Direction;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.command.MoveCommand;
import seoultech.se.core.command.RotateCommand;
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
import seoultech.se.core.event.TetrominoRotatedEvent;
import seoultech.se.core.event.TetrominoSpawnedEvent;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.TetrominoType;
import seoultech.se.core.result.LineClearResult;
import seoultech.se.core.result.LockResult;
import seoultech.se.core.result.MoveResult;
import seoultech.se.core.result.RotationResult;

/**
 * Command를 받아서 처리하고 Event를 발행하는 컨트롤러
 * 
 * 이 클래스의 역할이 크게 바뀌었습니다. 이전에는 moveLeft(), moveRight() 같은
 * 구체적인 메서드들을 제공했지만, 이제는 Command 패턴을 사용합니다.
 * 
 * 새로운 흐름:
 * 1. GameController가 Command 객체를 생성 (예: new MoveCommand(Direction.LEFT))
 * 2. executeCommand() 메서드로 Command 전달
 * 3. Command 타입에 따라 적절한 GameEngine 메서드 호출
 * 4. GameEngine이 반환한 Result를 Event로 변환
 * 5. Observer들에게 Event 전파
 * 
 * 왜 이렇게 바꿨나요?
 * 
 * 이전 방식의 문제점은 클라이언트와 서버가 다른 방식으로 동작한다는 것이었습니다.
 * 로컬에서는 메서드 호출로, 네트워크에서는 JSON으로 Command를 보내야 했죠.
 * 
 * 하지만 Command 패턴을 사용하면 두 가지 경우를 통일할 수 있습니다.
 * 로컬에서도 Command 객체를 만들고, 네트워크에서도 Command 객체를 만들면 됩니다.
 * (네트워크의 경우 JSON으로 직렬화하여 전송)
 * 
 * 이렇게 하면 나중에 멀티플레이어로 확장할 때, 클라이언트 코드를 거의 바꾸지 않아도 됩니다.
 * Command를 로컬 BoardController 대신 네트워크 서버로 보내기만 하면 되니까요.
 * 
 * 임시 역할 주의사항:
 * 
 * 이 BoardController는 임시로 Command 처리 역할을 하고 있습니다.
 * 나중에 GameService가 완성되면, 이 클래스의 executeCommand 로직을
 * GameService로 옮기게 됩니다. 그러면 BoardController는 완전히 제거되거나,
 * 아주 단순한 래퍼(wrapper)로만 남게 될 것입니다.
 */
@Getter
@Component
public class BoardController {
    private GameState gameState;
    private final List<BoardObserver> observers = new ArrayList<>();
    private final Random random = new Random();

    // 7-bag 시스템을 위한 상태
    private List<TetrominoType> currentBag = new ArrayList<>();
    private int bagIndex = 0;
    
    // 플레이 시간 추적
    private long gameStartTime;

    public BoardController(){
        this.gameState = new GameState(10, 20);
        this.gameStartTime = System.currentTimeMillis();
        initializeNextQueue();
    }

    // ========== Observer 관리 ==========
    
    public void addObserver(BoardObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BoardObserver observer) {
        observers.remove(observer);
    }
    
    // ========== Command 실행 (새로운 핵심 메서드) ==========
    
    /**
     * Command를 실행하고 발생한 Event들을 반환합니다
     * 
     * 이것이 이 클래스의 새로운 핵심 메서드입니다. 모든 게임 행동이
     * 이 메서드를 통과합니다. Command 타입에 따라 적절한 GameEngine 메서드를
     * 호출하고, Result를 분석하여 Event들을 생성합니다.
     * 
     * 왜 Event 리스트를 반환하나요?
     * 
     * 하나의 Command가 여러 Event를 발생시킬 수 있기 때문입니다.
     * 예를 들어 MoveDownCommand가 블록 고정으로 이어지면:
     * - TetrominoLockedEvent
     * - LineClearedEvent (라인이 지워졌다면)
     * - ScoreAddedEvent
     * - TetrominoSpawnedEvent (새 블록 생성)
     * 이렇게 여러 Event가 순서대로 발생합니다.
     * 
     * Event들은 발생 순서대로 Observer들에게 전달되어야 합니다.
     * 그래야 UI가 정확한 순서로 업데이트되니까요.
     * 
     * @param command 실행할 Command
     * @return 발생한 Event들의 리스트
     */
    public List<GameEvent> executeCommand(GameCommand command) {
        if (gameState.isGameOver()) {
            return List.of(); // 게임 오버 상태에서는 Command 무시
        }

        List<GameEvent> events = new ArrayList<>();

        // Command 타입에 따라 분기
        switch (command.getType()) {
            case MOVE:
                events.addAll(handleMoveCommand((MoveCommand) command));
                break;
                
            case ROTATE:
                events.addAll(handleRotateCommand((RotateCommand) command));
                break;
                
            case HARD_DROP:
                events.addAll(handleHardDropCommand());
                break;
                
            case HOLD:
                events.addAll(handleHoldCommand());
                break;
                
            case PAUSE:
                events.addAll(handlePauseCommand());
                break;
                
            case RESUME:
                events.addAll(handleResumeCommand());
                break;
                
            default:
                System.err.println("Unknown command type: " + command.getType());
        }

        // 생성된 Event들을 Observer들에게 전파
        for (GameEvent event : events) {
            notifyObservers(event);
        }

        return events;
    }

    // ========== Command 처리 메서드들 ==========
    
    /**
     * MoveCommand를 처리합니다
     * 
     * 이동 Command는 세 가지 방향이 있습니다: LEFT, RIGHT, DOWN.
     * 각 방향에 따라 적절한 GameEngine 메서드를 호출합니다.
     * 
     * 이동이 성공하면 TetrominoMovedEvent를 발생시킵니다.
     * DOWN 이동이 실패하면, 이것은 블록이 바닥에 닿았다는 의미이므로
     * 블록을 고정하고 새 블록을 생성하는 일련의 과정이 시작됩니다.
     */
    private List<GameEvent> handleMoveCommand(MoveCommand command) {
        List<GameEvent> events = new ArrayList<>();
        MoveResult result;

        switch (command.getDirection()) {
            case LEFT:
                result = GameEngine.tryMoveLeft(gameState);
                break;
            case RIGHT:
                result = GameEngine.tryMoveRight(gameState);
                break;
            case DOWN:
                result = GameEngine.tryMoveDown(gameState);
                break;
            default:
                return events; // 알 수 없는 방향
        }

        if (result.isSuccess()) {
            // 이동 성공
            gameState = result.getNewState();
            events.add(new TetrominoMovedEvent(
                gameState.getCurrentX(),
                gameState.getCurrentY(),
                gameState.getCurrentTetromino()
            ));
        } else if (command.getDirection() == Direction.DOWN) {
            // DOWN 이동 실패 = 블록 고정 필요
            events.addAll(lockAndSpawnNext());
        }
        // LEFT/RIGHT 이동 실패는 조용히 무시 (벽에 막힘)

        return events;
    }

    /**
     * RotateCommand를 처리합니다
     * 
     * 회전은 SRS Wall Kick 시스템을 사용합니다. GameEngine이 5가지 위치를
     * 시도하여 회전이 가능한지 판단합니다.
     * 
     * 성공하면 TetrominoRotatedEvent와 TetrominoMovedEvent를 함께 발생시킵니다.
     * 회전하면서 위치도 바뀔 수 있으니까요.
     * 
     * 실패하면 아무 Event도 발생시키지 않습니다. (조용히 무시)
     */
    private List<GameEvent> handleRotateCommand(RotateCommand command) {
        List<GameEvent> events = new ArrayList<>();
        
        RotationResult result = GameEngine.tryRotate(gameState, command.getDirection());

        if (result.isSuccess()) {
            gameState = result.getNewState();
            
            // 회전 성공 Event
            events.add(new TetrominoRotatedEvent(
                gameState.getCurrentTetromino(),
                command.getDirection(),
                result.getKickIndex(),
                gameState.getCurrentX(),
                gameState.getCurrentY()
            ));
            
            // 위치 변경 Event (회전으로 인한 위치 이동)
            events.add(new TetrominoMovedEvent(
                gameState.getCurrentX(),
                gameState.getCurrentY(),
                gameState.getCurrentTetromino()
            ));
        }
        // 회전 실패는 조용히 무시

        return events;
    }

    /**
     * HardDropCommand를 처리합니다
     * 
     * Hard Drop은 블록을 즉시 바닥까지 떨어뜨리고 고정합니다.
     * 이것은 가장 복잡한 Command 중 하나입니다. 왜냐하면 한 번에 여러 일이
     * 일어나기 때문입니다:
     * 
     * 1. 블록이 바닥까지 이동 (TetrominoMovedEvent)
     * 2. 블록 고정 (TetrominoLockedEvent)
     * 3. 라인 클리어 체크 (LineClearedEvent, ScoreAddedEvent)
     * 4. 새 블록 생성 (TetrominoSpawnedEvent)
     * 
     * GameEngine.hardDrop()은 이 모든 과정을 수행하고 LockResult를 반환합니다.
     * 우리는 이 LockResult를 여러 Event로 분해해야 합니다.
     */
    private List<GameEvent> handleHardDropCommand() {
        List<GameEvent> events = new ArrayList<>();
        
        LockResult result = GameEngine.hardDrop(gameState);
        gameState = result.getNewState();
        
        // 최종 위치로 이동한 Event
        events.add(new TetrominoMovedEvent(
            gameState.getCurrentX(),
            gameState.getCurrentY(),
            gameState.getCurrentTetromino()
        ));
        
        // 고정 및 후속 처리
        events.addAll(processLockResult(result));
        
        return events;
    }

    /**
     * HoldCommand를 처리합니다
     * 
     * Hold 기능은 현재 블록을 보관하고 보관된 블록이 있으면 교체합니다.
     * 한 턴에 한 번만 사용 가능합니다.
     */
    private List<GameEvent> handleHoldCommand() {
        List<GameEvent> events = new ArrayList<>();
        
        seoultech.se.core.result.HoldResult result = GameEngine.tryHold(gameState);
        
        if (result.isSuccess()) {
            gameState = result.getNewState();
            
            // Hold 변경 Event
            events.add(new seoultech.se.core.event.HoldChangedEvent(
                result.getNewHeldPiece(),
                result.getPreviousHeldPiece()
            ));
            
            // 테트로미노 스폰 Event (새 블록 또는 교체된 블록)
            events.add(new TetrominoSpawnedEvent(
                gameState.getCurrentTetromino(),
                gameState.getCurrentX(),
                gameState.getCurrentY()
            ));
            
            // 위치 이동 Event
            events.add(new TetrominoMovedEvent(
                gameState.getCurrentX(),
                gameState.getCurrentY(),
                gameState.getCurrentTetromino()
            ));
            
            // Next Queue 업데이트 Event (Hold가 비어있던 경우)
            if (result.getPreviousHeldPiece() == null) {
                events.add(new seoultech.se.core.event.NextQueueUpdatedEvent(
                    gameState.getNextQueue()
                ));
            }
            
            // GameState 변경 Event
            events.add(new GameStateChangedEvent(gameState));
            
        } else {
            // Hold 실패 Event
            events.add(new seoultech.se.core.event.HoldFailedEvent(
                result.getFailureReason()
            ));
        }
        
        return events;
    }

    /**
     * PauseCommand를 처리합니다
     * 
     * 일시정지는 GameState의 플래그만 바꿉니다.
     * 실제 게임 루프 중단은 GameController에서 처리합니다.
     */
    private List<GameEvent> handlePauseCommand() {
        List<GameEvent> events = new ArrayList<>();
        
        if (!gameState.isPaused()) {
            GameState newState = gameState.deepCopy();
            newState.setPaused(true);
            gameState = newState;
            
            events.add(new seoultech.se.core.event.GamePausedEvent());
        }
        
        return events;
    }

    /**
     * ResumeCommand를 처리합니다
     * 
     * 일시정지 해제도 마찬가지로 플래그만 바꿉니다.
     */
    private List<GameEvent> handleResumeCommand() {
        List<GameEvent> events = new ArrayList<>();
        
        if (gameState.isPaused()) {
            GameState newState = gameState.deepCopy();
            newState.setPaused(false);
            gameState = newState;
            
            events.add(new seoultech.se.core.event.GameResumedEvent());
        }
        
        return events;
    }

    // ========== 블록 고정 및 생성 ==========
    
    /**
     * 블록을 고정하고 새 블록을 생성합니다
     * 
     * 이 메서드는 DOWN 이동이 실패했을 때 호출됩니다.
     * GameEngine.lockTetromino()를 호출하여 현재 블록을 보드에 고정하고,
     * 라인 클리어를 처리한 후, 새 블록을 생성합니다.
     */
    private List<GameEvent> lockAndSpawnNext() {
        List<GameEvent> events = new ArrayList<>();
        
        LockResult result = GameEngine.lockTetromino(gameState);
        gameState = result.getNewState();
        
        events.addAll(processLockResult(result));
        
        return events;
    }

    /**
     * LockResult를 처리하고 적절한 Event들을 생성합니다
     * 
     * 이 메서드가 Result → Event 변환의 핵심입니다.
     * LockResult는 GameEngine의 언어이고, Event는 도메인의 언어입니다.
     * 
     * LockResult에는 다음 정보가 포함되어 있습니다:
     * - 게임 오버 여부
     * - LineClearResult (지워진 라인 정보)
     * - 새로운 GameState
     * 
     * 우리는 이것을 여러 Event로 분해합니다:
     * - TetrominoLockedEvent: 블록이 고정되었다
     * - LineClearedEvent: 라인이 지워졌다 (있다면)
     * - ScoreAddedEvent: 점수를 얻었다 (있다면)
     * - GameOverEvent: 게임이 끝났다 (게임 오버라면)
     * - TetrominoSpawnedEvent: 새 블록이 생성되었다 (게임 오버가 아니라면)
     * - GameStateChangedEvent: 게임 상태가 변경되었다
     */
    private List<GameEvent> processLockResult(LockResult result) {
        List<GameEvent> events = new ArrayList<>();

        // 1. 블록 고정 Event
        events.add(new TetrominoLockedEvent(
            gameState.getCurrentTetromino(),
            gameState.getCurrentX(),
            gameState.getCurrentY()
        ));

        // 2. 게임 오버 체크
        if (result.isGameOver()) {
            long playTimeMillis = System.currentTimeMillis() - gameStartTime;
            events.add(new GameOverEvent(
                result.getGameOverReason(),
                gameState.getScore(),
                gameState.getLevel(),
                gameState.getLinesCleared(),
                playTimeMillis
            ));
            
            // GameState 업데이트 Event
            events.add(new GameStateChangedEvent(gameState));
            
            return events; // 게임 오버면 여기서 종료
        }

        // 3. 라인 클리어 처리
        LineClearResult clearResult = result.getLineClearResult();
        if (clearResult.getLinesCleared() > 0) {
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
        } else {
            // 라인을 지우지 못했으면 콤보 종료
            if (gameState.getComboCount() > 0) {
                events.add(new ComboBreakEvent(gameState.getComboCount()));
            }
        }

        // 4. GameState 변경 Event
        events.add(new GameStateChangedEvent(gameState));

        // 5. 새 블록 생성
        spawnNewTetromino();
        events.add(new TetrominoSpawnedEvent(
            gameState.getCurrentTetromino(),
            gameState.getCurrentX(),
            gameState.getCurrentY()
        ));
        
        // 새 블록의 위치 Event
        events.add(new TetrominoMovedEvent(
            gameState.getCurrentX(),
            gameState.getCurrentY(),
            gameState.getCurrentTetromino()
        ));

        return events;
    }

    // ========== 7-bag 시스템 ==========
    
    /**
     * 새로운 테트로미노를 생성합니다
     * 
     * 7-bag 알고리즘을 사용합니다. 7가지 블록을 가방에 넣고 섞은 다음,
     * 하나씩 꺼내서 사용합니다. 가방이 비면 다시 채우고 섞습니다.
     * 
     * 이렇게 하면 같은 블록이 연속으로 너무 많이 나오는 것을 방지할 수 있습니다.
     */
    private void spawnNewTetromino() {
        TetrominoType nextType = getNextTetrominoType();

        Tetromino newTetromino = new Tetromino(nextType);
        gameState.setCurrentTetromino(newTetromino);
        gameState.setCurrentX(gameState.getBoardWidth() / 2 - 1);
        gameState.setCurrentY(0);
    }

    private TetrominoType getNextTetrominoType() {
        if (currentBag.isEmpty() || bagIndex >= currentBag.size()) {
            refillBag();
        }

        TetrominoType nextType = currentBag.get(bagIndex);
        bagIndex++;
        updateNextQueue();
        return nextType;
    }

    private void refillBag() {
        currentBag.clear();
        bagIndex = 0;

        for (TetrominoType type : TetrominoType.values()) {
            currentBag.add(type);
        }

        // 셔플 (Fisher-Yates 알고리즘)
        for (int i = currentBag.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            TetrominoType temp = currentBag.get(i);
            currentBag.set(i, currentBag.get(j));
            currentBag.set(j, temp);
        }
    }

    private void initializeNextQueue() {
        refillBag();
        updateNextQueue();
        spawnNewTetromino();
    }

    private void updateNextQueue() {
        TetrominoType[] queue = new TetrominoType[6];

        for (int i = 0; i < 6; i++) {
            int index = bagIndex + i;

            if (index < currentBag.size()) {
                queue[i] = currentBag.get(index);
            } else {
                // 다음 가방의 블록 예측 (아직 섞이지 않았으므로 순서대로)
                int nextBagIndex = index - currentBag.size();
                if (nextBagIndex < 7) {
                    queue[i] = TetrominoType.values()[nextBagIndex % 7];
                }
            }
        }
        
        gameState.setNextQueue(queue);
    }

    // ========== 헬퍼 메서드들 ==========
    
    private String getScoreReason(LineClearResult result) {
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

    private String lineCountToName(int lines) {
        switch (lines) {
            case 1: return "SINGLE";
            case 2: return "DOUBLE";
            case 3: return "TRIPLE";
            case 4: return "TETRIS";
            default: return "UNKNOWN";
        }
    }

    // ========== Observer 알림 ==========
    
    /**
     * Event를 Observer들에게 전파합니다
     * 
     * Event 타입에 따라 적절한 Observer 메서드를 호출합니다.
     * 이것은 Event를 실제 UI 업데이트로 변환하는 마지막 단계입니다.
     * 
     * 왜 Event별로 다른 메서드를 호출하나요?
     * 
     * BoardObserver 인터페이스는 Event 시스템이 도입되기 전에 만들어졌기 때문에,
     * onTetrominoMoved(), onLineCleared() 같은 구체적인 메서드들을 가지고 있습니다.
     * 
     * 이상적으로는 onEvent(GameEvent event) 하나의 메서드만 있으면 좋겠지만,
     * 기존 코드와의 호환성을 위해 이렇게 변환 레이어를 둡니다.
     * 
     * 나중에 BoardObserver를 리팩토링할 때 단일 메서드로 통일할 수 있습니다.
     */
    private void notifyObservers(GameEvent event) {
        switch (event.getType()) {
            case TETROMINO_MOVED:
                TetrominoMovedEvent movedEvent = (TetrominoMovedEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onTetrominoMoved(
                        movedEvent.getNewX(),
                        movedEvent.getNewY(),
                        movedEvent.getTetromino()
                    );
                }
                break;

            case TETROMINO_ROTATED:
                TetrominoRotatedEvent rotatedEvent = (TetrominoRotatedEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onTetrominoRotated(
                        rotatedEvent.getDirection(),
                        rotatedEvent.getKickIndex(),
                        rotatedEvent.getTetromino()
                    );
                }
                break;

            case TETROMINO_LOCKED:
                TetrominoLockedEvent lockedEvent = (TetrominoLockedEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onTetrominoLocked(lockedEvent.getTetromino());
                }
                break;

            case TETROMINO_SPAWNED:
                TetrominoSpawnedEvent spawnedEvent = (TetrominoSpawnedEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onTetrominoSpawned(spawnedEvent.getTetromino());
                }
                break;

            case LINE_CLEARED:
                LineClearedEvent clearedEvent = (LineClearedEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onLineCleared(
                        clearedEvent.getLinesCleared(),
                        clearedEvent.getClearedRows(),
                        clearedEvent.isTSpin(),
                        clearedEvent.isTSpinMini(),
                        clearedEvent.isPerfectClear()
                    );
                }
                break;

            case SCORE_ADDED:
                ScoreAddedEvent scoreEvent = (ScoreAddedEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onScoreAdded(
                        scoreEvent.getPoints(),
                        scoreEvent.getReason()
                    );
                }
                break;

            case GAME_STATE_CHANGED:
                GameStateChangedEvent stateEvent = (GameStateChangedEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onGameStateChanged(stateEvent.getGameState());
                }
                break;

            case GAME_OVER:
                GameOverEvent gameOverEvent = (GameOverEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onGameOver(gameOverEvent.getReason());
                }
                break;

            // 아직 처리하지 않는 Event 타입들
            default:
                // 조용히 무시 (나중에 필요하면 추가)
                break;
        }
    }
}
