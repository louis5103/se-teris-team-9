package seoultech.se.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import lombok.Getter;
import seoultech.se.client.mapper.EventMapper;
import seoultech.se.core.BoardObserver;
import seoultech.se.core.GameEngine;
import seoultech.se.core.GameState;
import seoultech.se.core.command.Direction;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.command.MoveCommand;
import seoultech.se.core.command.RotateCommand;
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
    private List<TetrominoType> nextBag = new ArrayList<>();  // 다음 가방 미리 준비
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

        // 일시정지 상태에서는 PAUSE/RESUME 명령만 허용
        if (gameState.isPaused() && 
            command.getType() != seoultech.se.core.command.CommandType.RESUME &&
            command.getType() != seoultech.se.core.command.CommandType.PAUSE) {
            return List.of(); // 일시정지 중이면 다른 명령 무시
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
                result = GameEngine.tryMoveDown(gameState, command.isSoftDrop());
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
            
            // Hold가 비어있었던 경우, Next Queue를 7-bag 시스템으로 업데이트
            // GameEngine.tryHold()는 nextQueue[0]을 읽기만 하고 제거하지 않으므로
            // 여기서 명시적으로 큐를 업데이트하여 7-bag 시스템과 동기화합니다
            if (result.getPreviousHeldPiece() == null) {
                updateNextQueue();
            }
            
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
            // Hold 실패 처리
            // 게임 오버 상태인지 확인
            if (gameState.isGameOver()) {
                // 게임 오버 Event 발생
                events.add(new GameOverEvent(result.getFailureReason(), 
                    gameState.getScore(), 
                    gameState.getLevel(), 
                    gameState.getLinesCleared(), 
                    (System.currentTimeMillis() - gameStartTime) / 1000));
            } else {
                // 일반 Hold 실패 (이미 사용함 등)
                events.add(new seoultech.se.core.event.HoldFailedEvent(
                    result.getFailureReason()
                ));
            }
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
     * 이제 EventMapper를 사용하여 Result → Event 변환을 수행합니다.
     * 이를 통해 BoardController의 복잡도가 크게 감소했습니다.
     * 
     * @param result 고정 결과
     * @return 발생한 이벤트들의 리스트
     */
    private List<GameEvent> processLockResult(LockResult result) {
        // EventMapper를 사용하여 LockResult를 Event 리스트로 변환
        List<GameEvent> events = EventMapper.fromLockResult(
            result,
            gameState,
            gameStartTime
        );

        // 게임 오버가 아니면 새 블록 생성
        if (!result.isGameOver()) {
            spawnNewTetromino();
            events.addAll(EventMapper.createTetrominoSpawnEvents(gameState));
        }

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
            // 현재 가방이 비었으면 다음 가방을 현재 가방으로 교체
            currentBag = nextBag;
            nextBag = createAndShuffleBag();
            bagIndex = 0;
        }

        TetrominoType nextType = currentBag.get(bagIndex);
        bagIndex++;
        updateNextQueue();
        return nextType;
    }

    /**
     * 새로운 7-bag을 생성하고 섞습니다
     */
    private List<TetrominoType> createAndShuffleBag() {
        List<TetrominoType> bag = new ArrayList<>();
        
        // 7가지 블록을 모두 추가
        for (TetrominoType type : TetrominoType.values()) {
            bag.add(type);
        }

        // Fisher-Yates 셔플
        for (int i = bag.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            TetrominoType temp = bag.get(i);
            bag.set(i, bag.get(j));
            bag.set(j, temp);
        }
        
        return bag;
    }

    private void refillBag() {
        currentBag = createAndShuffleBag();
        nextBag = createAndShuffleBag();  // 다음 가방도 미리 준비
        bagIndex = 0;
    }

    private void initializeNextQueue() {
        refillBag();
        updateNextQueue();
        spawnNewTetromino();
    }

    /**
     * Next Queue를 업데이트합니다
     * 
     * 현재 가방과 다음 가방에서 정확하게 6개의 블록을 미리보기로 제공합니다.
     * 이제 다음 가방이 미리 준비되어 있으므로 정확한 예측이 가능합니다.
     */
    private void updateNextQueue() {
        TetrominoType[] queue = new TetrominoType[6];

        for (int i = 0; i < 6; i++) {
            int index = bagIndex + i;

            if (index < currentBag.size()) {
                // 현재 가방에서 가져오기
                queue[i] = currentBag.get(index);
            } else {
                // 다음 가방에서 가져오기 (이미 섞여있음)
                int nextBagIndex = index - currentBag.size();
                if (nextBagIndex < nextBag.size()) {
                    queue[i] = nextBag.get(nextBagIndex);
                }
            }
        }
        
        gameState.setNextQueue(queue);
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
                
            case LEVEL_UP:
                seoultech.se.core.event.LevelUpEvent levelUpEvent = (seoultech.se.core.event.LevelUpEvent) event;
                for (BoardObserver observer : observers) {
                    observer.onLevelUp(levelUpEvent.getNewLevel());
                }
                break;

            case GAME_PAUSED:
                for (BoardObserver observer : observers) {
                    observer.onGamePaused();
                }
                break;

            case GAME_RESUMED:
                for (BoardObserver observer : observers) {
                    observer.onGameResumed();
                }
                break;

            // 아직 처리하지 않는 Event 타입들
            default:
                // 조용히 무시 (나중에 필요하면 추가)
                break;
        }
    }
}
