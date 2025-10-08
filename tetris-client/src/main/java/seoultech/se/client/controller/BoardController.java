package seoultech.se.client.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;

import lombok.Getter;
import seoultech.se.core.GameEngine;
import seoultech.se.core.GameState;
import seoultech.se.core.model.BoardObserver;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;
import seoultech.se.core.model.enumType.TetrominoType;
import seoultech.se.core.model.result.LineClearResult;
import seoultech.se.core.model.result.LockResult;
import seoultech.se.core.model.result.MoveResult;
import seoultech.se.core.model.result.RotationResult;

/**
 * 게임 상태를 관리하고 Observer들에게 알림을 보내는 컨트롤러
 * 
 * 1. GameState 보관: 현재 게임의 상태를 메모리에 유지합니다
 * 2. 명령 실행 조율: Command를 받아 GameEngine에 전달하고 결과를 받습니다
 * 3. 이벤트 발행: Result를 분석하여 Observer들에게 적절한 이벤트를 알립니다
 * 
 */
@Getter
@Component
public class BoardController {
    private GameState gameState;
    private final List<BoardObserver> observers = new ArrayList<>();
    private final Random random = new Random();

    private List<TetrominoType> currentBag = new ArrayList<>();
    private int bagIndex = 0;

    public BoardController(){
        this.gameState = new GameState(10, 20);
        initializeNextQueue();
    }

    // ========== Observer 관리 ==========
    public void addObserver(BoardObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(BoardObserver observer) {
        observers.remove(observer);
    }
    

    // ========== 이동 명령 ==========
    public void moveLeft() {
        if(gameState.isGameOver()) return;

        MoveResult result = GameEngine.tryMoveLeft(gameState);

        if(result.isSuccess()) {
            gameState = result.getNewState();
            notifyTetrominoMoved();
        }
    }

    public void moveRight() {
        if(gameState.isGameOver()) return;

        MoveResult result = GameEngine.tryMoveRight(gameState);

        if(result.isSuccess()) {
            gameState = result.getNewState();
            notifyTetrominoMoved();
        }
    } 

    public void moveDown() {
        if(gameState.isGameOver()) return;

        MoveResult result = GameEngine.tryMoveDown(gameState);

        if(result.isSuccess()) {
            gameState = result.getNewState();
            notifyTetrominoMoved();
        } else {
            lockAndSpawnNext();
        }
    }

    // ========== 회전 명령 ==========
    public void rotateClockwise() {
        if(gameState.isGameOver()) return;

        RotationResult result = GameEngine.tryRotate(gameState, RotationDirection.CLOCKWISE);

        if(result.isSuccess()) {
            gameState = result.getNewState();
            notifyTetrominoRotated(result.getDirection(), result.getKickIndex());
            notifyTetrominoMoved();
        } else {
            notifyTetrominoRotationFailed(result.getDirection());
        }
    }

    public void rotateCounterClockwise() {
        if(gameState.isGameOver()) return;

        RotationResult result = GameEngine.tryRotate(gameState, RotationDirection.COUNTER_CLOCKWISE);

        if(result.isSuccess()) {
            gameState = result.getNewState();
            notifyTetrominoRotated(result.getDirection(), result.getKickIndex());
            notifyTetrominoMoved();
        } else {
            notifyTetrominoRotationFailed(result.getDirection());
        }
    }

    // ========== Hard Drop ==========
    public void hardDrop() {
        if (gameState.isGameOver()) return;

        LockResult result = GameEngine.hardDrop(gameState);
        gameState = result.getNewState();
        notifyTetrominoMoved(); // 최종위치
        processLockResult(result);
    }

    // ========== Hold ==========
    public void hold() {
        // TODO : Hold 기능 구현.
        notifyHoldFailed();
    }

    // ========== 블록 고정 및 생성 ==========
    /**
     * 1. 현재 블록을 보드에 고정
     * 2. 라인 클리어 체크
     * 3. 점수 계산
     * 4. 게임 오버 체크
     * 5. 새 블록 생성
     */
    private void lockAndSpawnNext() {
        LockResult result = GameEngine.lockCurrentTetromino(gameState);
        gameState = result.getNewState();

        processLockResult(result);
    }

    /**
     * LockResult를 처리하고 적절한 이벤트를 발행
     * lockAndSpawnNext와 hardDrop 에서 사용
     * esult를 분석하여 여러 이벤트로 변환하
     */
    private void processLockResult(LockResult result) {
        // 1. 블록 고정 알림
        notifyTetromiLocked();

        // 2. 게임 오버 체크
        if(result.isGameOver()) {
            notifyGameOver(result.getGameOverReason());
            return;
        }

        // 3. 라인 클리어 처리
        LineClearResult clearResult = result.getLineClearResult();
        if (clearResult.getClearedLines() > 0) {
            notifyLineCleared(clearResult);
            notifyScoreAdded(clearResult.getScoreEarned(), getScoreReason(clearResult));

            // 콤보 알림
            if(gameState.getComboCount() > 0) {
                notifyCombo(gameState.getComboCount());
            }

            // B2B 알림
            if(gameState.getBackToBackCount() > 0) {
                notifyBackToBack(gameState.getBackToBackCount());
            } else {
                // 라인 지우지 못할 때, 콤보 종료
                if(gameState.getComboCount() > 0) {
                    notifyComboBreak(gameState.getComboCount());
                }
            }
        }   

        // 4. 게임 상태 변경 알림.
        notifyGameStateChanged();

        // 5. 새 블록 생성
        spawnNewTetromino();
    }

    // 7-bag System 적용
    private void spawnNewTetromino() {
        TetrominoType nextType = getNextTetrominoType();

        Tetromino newTetromino = new Tetromino(nextType);
        gameState.setCurrentTetromino(newTetromino);
        gameState.setCurrentX(gameState.getBoardWidth() / 2 - 1);
        gameState.setCurrentY(0);

        notifyTetrominoSpawned(newTetromino);
        notifyTetrominoMoved();
    }

    // 7-bag 알고리즘
    private TetrominoType getNextTetrominoType() {
        if(currentBag.isEmpty() || bagIndex >= currentBag.size()) {
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

        for(TetrominoType type : TetrominoType.values()) {
            currentBag.add(type);
        }

        // 셔플
        for(int i = currentBag.size() - 1; i > 0; i--) {
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

        for(int i = 0; i < 6; i++) {
            int index = bagIndex + i;

            if(index < currentBag.size()) {
                queue[i] = currentBag.get(index);
            } else {
                int nextBagIndex = index - currentBag.size();

                // TODO : 다음 가방 순서를 미리 생성한 후 제공.
                if(nextBagIndex < 7) {
                    queue[i] = TetrominoType.values()[nextBagIndex % 7];
                }
            }
        }
        gameState.setNextQueue(queue);
        notifyNextQueueUpdated();
    }

    private String getScoreReason(LineClearResult result) {
        if(result.isPerfectClear()) {
            return "Perfect Clear";
        }
        if(result.isTSpin()) {
            if(result.isTSpinMini()) {
                return "T-Spin Mini " + lineCountToName(result.getClearedLines());
            } else {
                return "T-Spin " + lineCountToName(result.getClearedLines());
            }
        }
        return lineCountToName(result.getLinesCleared());
    }

    private String lineCountToName(int lines) {
        switch(lines) {
            case 1: return "Single";
            case 2: return "Double";
            case 3: return "Triple";
            case 4: return "Tetris";
            default: return "UNKNOWN";
        }
    }

    // ========== Observer 알림 메서드들 ==========
    private void notifyTetrominoMoved() {
        for(BoardObserver observer : observers) {
            observer.onTetrominoMoved(
                gameState.getCurrentX(),
                gameState.getCurrentY(),
                gameState.getCurrentTetromino()
            );
        }
    }

    private void notifyTetrominoRotated(RotationDirection direction, int kickIndex) {
        for(BoardObserver observer : observers) {
            observer.onTetrominoRotated(direction, kickIndex, gameState.getCurrentTetromino());
        }
    }

    private void notifyTetrominoRotationFailed(RotationDirection direction) {
        for(BoardObserver observer : observers) {
            observer.onTetrominoRotationFailed(direction, gameState.getCurrentTetromino());
        }

    }

    private void notifyTetromiLocked() {
        for(BoardObserver observer : observers) {
            observer.onTetrominoLocked(gameState.getCurrentTetromino());
        }
    }

    private void notifyTetrominoSpawned(Tetromino tetromino) {
        for(BoardObserver observer : observers) {
            observer.onTetrominoSpawned(tetromino);
        }
    }

    private void notifyNextQueueUpdated(TetrominoType[] queue) {
        for(BoardObserver observer : observers) {
            observer.onNextQueueUpdated(queue);
        }
    }

    private void notifyLineCleared(LineClearResult result) {
        for(BoardObserver observer : observers) {
            observer.onLineCleared(
                result.getClearedLines(), 
                result.getClearedRows(),
                result.isTSpin(),
                result.isTSpinMini(),
                result.isPerfectClear()
            );
        }
    }

    private void notifyScoreAdded(long points, String reason) {
        for(BoardObserver observer : observers) {
            observer.onScoreAdded(points, reason);
        }
    }

    private void notifyGameStateChanged() {
        for(BoardObserver observer : observers) {
            observer.onGameStateChanged(gameState);
        }
    }

    private void GameOver(String reason) {
        for(BoardObserver observer : observers) {
            observer.onGameOver(reason);
        }
    }

    private void notifyCombo(int comboCount) {
        for(BoardObserver observer : observers) {
            observer.onCombo(comboCount);
        }
    }

    private void notifyComboBreak(int finalComboCount) {
        for(BoardObserver observer : observers) {
            observer.onComboBreak(finalComboCount);
        }
    }

    private void notifyBackToBack(int backToBackCount) {
        for(BoardObserver observer : observers) {
            observer.onBackToBack(backToBackCount);
        }
    }

    private void notifyHoldFailed() {
        for(BoardObserver observer : observers) {
            observer.onHoldFailed();
        }
    }

}
