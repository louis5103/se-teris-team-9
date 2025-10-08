package seoultech.se.core;

import java.util.List;

import seoultech.se.core.model.Cell;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;
import seoultech.se.core.model.enumType.TetrominoType;
import seoultech.se.core.model.enumType.WallKickEventData;
import seoultech.se.core.model.result.LineClearResult;
import seoultech.se.core.model.result.LockResult;
import seoultech.se.core.model.result.MoveResult;
import seoultech.se.core.model.result.RotationResult;

/**
 * 게임 엔진 클래스
 * Input, Output: GameState
 * 기능: 블록 이동, 회전, 고정 등 게임 내 주요 로직 처리
 * 각 메서드는 새로운 GameState 객체를 반환하여 불변성을 유지
 * 각 메서드는 성공 여부와 함께 새로운 상태를 포함하는 결과 객체를 반환
 */
public class GameEngine {
    public static MoveResult tryMoveLeft(GameState state) {
        int newX = state.getCurrentX() - 1;

        if(isValidPosition(state, state.getCurrentTetromino(), newX, state.getCurrentY())) {
            GameState newState = state.deepCopy();
            newState.setCurrentX(newX);
            return MoveResult.success(newState);
        }
        return MoveResult.failed(state, "[GameEngine] (Method: tryMoveLeft) Cannot move left : Blocked or out of bounds");
    }

    // ========== 이동 관련 메서드 ==========

    public static MoveResult tryMoveRight(GameState state) {
        int newX = state.getCurrentX() + 1;

        if(isValidPosition(state, state.getCurrentTetromino(), newX, state.getCurrentY())) {
            GameState newState = state.deepCopy();
            newState.setCurrentX(newX);
            return MoveResult.success(newState);
        }
        return MoveResult.failed(state, "[GameEngine] (Method: tryMoveRight) Cannot move right : Blocked or out of bounds");
    }

    //TODO: JavaDoc 설명 구현.
    /**
     * 아래로 이동을 시도합니다
     * 
     * 이동할 수 없으면 고정(lock)이 필요하다는 신호입니다.
     * 하지만 이 메서드는 고정을 수행하지 않습니다.
     * 호출자가 MoveResult를 보고 lockTetromino()를 호출해야 합니다.
     */
    public static MoveResult tryMoveDown(GameState state) {
        int newY = state.getCurrentY() + 1;

        if(isValidPosition(state, state.getCurrentTetromino(), state.getCurrentX(), newY)) {
            GameState newState = state.deepCopy();
            newState.setCurrentY(newY);
            return MoveResult.success(newState);
        }
        return MoveResult.failed(state, "[GameEngine] (Method: tryMoveDown) Cannot move down : Blocked or out of bounds");
    }

    // ========== 회전 관련 메서드 ==========
    
    /**
     * 회전을 시도합니다 (SRS Wall Kick 포함)
     * 
     * SRS(Super Rotation System)는 현대 테트리스의 표준 회전 시스템입니다.
     * 단순히 회전만 하는 것이 아니라, 벽이나 다른 블록에 막혔을 때
     * 자동으로 위치를 조정하여 회전을 성공시키려고 시도합니다.
     * 
     * 5가지 위치를 순서대로 시도하며, 하나라도 성공하면 회전이 완료됩니다.
     * 
     * @param state 현재 게임 상태
     * @param direction 회전 방향 (시계/반시계)
     * @return 회전 결과 (성공/실패, kickIndex 포함)
     */
    public static RotationResult tryRotate(GameState state, RotationDirection direction) {
        // O 블록 : 회전하진 않음.
        if(state.getCurrentTetromino().getType() == TetrominoType.O) {
            return RotationResult.success(state, direction, 0);
        }

        Tetromino rotated = state.getCurrentTetromino().getRotatedInstance(direction);

        // 월킥 데이터 가져오기
        int[][] kickData = WallKickEventData.getKickData(
                state.getCurrentTetromino().getType(),
                state.getCurrentTetromino().getRotationState(),
                rotated.getRotationState()
        );

        // 월킥 시도
        for(int kickIndex = 0; kickIndex < kickData.length; kickIndex++) {
            int[] offset = kickData[kickIndex];
            int newX = state.getCurrentX() + offset[0];
            int newY = state.getCurrentY() + offset[1];

            if(isValidPosition(state, rotated, newX, newY)) {
                GameState newState = state.deepCopy();
                newState.setCurrentTetromino(rotated);
                newState.setCurrentX(newX);
                newState.setCurrentY(newY);
                return RotationResult.success(newState, direction, kickIndex);
            }
        }
        return RotationResult.failed(state, "[GameEngine] (Method: tryRotate) Cannot rotate : Blocked or out of bounds. wall kicks failed.");
    }

    // ========== Hard Drop ==========
    
    /**
     * Hard Drop을 실행합니다
     * 
     * 블록을 즉시 바닥까지 떨어뜨리고 고정합니다.
     * 이 메서드는 두 단계를 합친 것입니다:
     * 1. 바닥까지 이동
     * 2. 즉시 고정 (lockTetromino 호출)
     * 
     * @return LockResult (라인 클리어 포함)
     */
    public static LockResult hardDrop(GameState state){
        // 1. 바닥까지 이동.
        GameState droppedState = state.deepCopy();
        int dropDistance = 0;

        while(isValidPosition(droppedState, droppedState.getCurrentTetromino(), 
                              droppedState.getCurrentX(), droppedState.getCurrentY() + 1)
        ) {
            droppedState.setCurrentY(droppedState.getCurrentY() + 1);
            dropDistance++;
        }

        // Hard Drop 점수 추가. (1칸당 2점)
        droppedState.addScore(dropDistance * 2);

        // 2. 즉시 고정.
        return lockTetromino(droppedState);
    }

    // ========== 테트로미노 고정 ==========
    
    /**
     * 테트로미노를 보드에 고정하고 라인 클리어를 처리합니다
     * 
     * 이 메서드는 여러 단계를 거칩니다:
     * 1. 테트로미노의 각 블록을 grid에 추가
     * 2. 게임 오버 체크 (spawn 위치를 벗어나면 게임 오버)
     * 3. 라인 클리어 체크 및 실행
     * 4. 점수 계산
     * 5. Hold 재사용 가능하게 설정
     * 
     * @param state 현재 게임 상태
     * @return 고정 결과 (게임 오버 여부, 라인 클리어 정보 포함)
     */
    public static LockResult lockTetromino(GameState state) {
        GameState newState = state.deepCopy();

        // 1. Grid에 테트로미노 고정
        int[][] shape = state.getCurrentTetromino().getCurrentShape();

        for(int row = 0; row < shape.length; row++) {
            for(int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    int absX = state.getCurrentX() + (col - state.getCurrentTetromino().getPivotX());
                    int absY = state.getCurrentY() + (row - state.getCurrentTetromino().getPivotY());

                    // 게임 오버 체크. 
                    if( absY < 0 ) {
                        // 게임 오버 처리
                        newState.setGameOver(true);
                        return LockResult.gameOver(newState, "[GameEngine] (Method: lockTetromino) Game Over: Block locked above the board.");
                    }
                    // 셀에 색상 채우기
                    if(absY >= 0 && absY < state.getBoardHeight() &&
                       absX >= 0 && absX < state.getBoardWidth()
                    ) {
                        newState.getGrid()[absY][absX].setColor(state.getCurrentTetromino().getColor());
                        newState.getGrid()[absY][absX].setOccupied(true);
                    }
                }
            }
        }

        // 2. 라인 클리어 체크 및 실행
        LineClearResult clearResult = checkAndClearLines(newState);

        // 3. 점수 및 통계 업데이트
        if(clearResult.getLinesCleared() > 0) {
            newState.addScore(clearResult.getScoreEarned());
            newState.addLinesCleared(clearResult.getLinesCleared());

            // 콤보 업데이트
            newState.setComboCount(newState.getComboCount() + 1);
            newState.setLastActionClearedLines(true);

            // B2B 업데이트 
            boolean isDifficult = clearResult.getLinesCleared() == 4 || clearResult.isTSpin();
            if(isDifficult && newState.isLastClearedWasDifficult()) {
                newState.setBackToBackCount(newState.getBackToBackCount() + 1);
            } else if (isDifficult) {
                newState.setBackToBackCount(1);
            } else {
                newState.setBackToBackCount(0);
            }
            newState.setLastClearWasDifficult(isDifficult);;
        } else { // 라인 클리어 못했으면 콤보 초기화
            newState.setComboCount(0);
            newState.setLastActionClearedLines(false);
            newState.setBackToBackCount(0);
            newState.setLastClearWasDifficult(false);
        }

        // 4. Hold 재사용 가능하게 설정.
        newState.setHoldUsedThisTurn(false);
        return LockResult.success(newState, clearResult);
    }

    // ========== 라인 클리어 ===================
    private static LineClearResult checkAndClearLines(GameState state) {
        List<Integer> clearedRowsList = new java.util.ArrayList<>();

        // 라인 체크
        for (int row = state.getBoardHeight() - 1; row >= 0; row--) {
            boolean isFullLine = true;

            for(int col = 0; col < state.getBoardWidth(); coll++) {
                if(!state.getGrid()[row][col].isOccupied()) {
                    isFullLine = false;
                    break;
                }
            }

            if (isFullLine) {
                clearedRowsList.add(row);
            }
        }

        if (clearedRowsList.isEmpty()){
            return LineClearResult.none();
        }

        // 라인 클리어 실행. (라인 지우기 + 위 블록들 아래로 내리기)
        for(int clearedRow : clearedRowsList){
            // 해당 줄 위 모든 라인 내리기
            for (int row = clearedRow; row > 0; row--) {
                for(int col = 0; col <state.getBoardWidth(); col++) {
                    state.getGrid()[row][col] = state.getGrid()[row - 1][col].copy();
                }
            }

            // 최상단 줄은 빈 칸으로 초기화
            for (int col = 0; col < state.getBoardWidth(); col++) {
                state.getGrid()[0][col] = Cell.empty();
            }
        }

        int linesCleared = clearedRowsList.size();
        int[] clearedRows = clearedRowsList.stream().mapToInt(i -> i).toArray();

        // Perfect clear 체크
        boolean isPerfectClear = checkPerfectClear(state);

        // TODO: T-Spin 감지 구현 필요
        boolean isTSpin = false;
        boolean isTSpinMini = false;

        // 점수 계산
        long score = calculateScore(linesCleared, isTSpin, isTSpinMini, isPerfectClear,
                state.getBackToBackCount(), state.getComboCount()
        );

        if(isTSpin) {
            return LineClearResult.tSpin(linesCleared, clearedRows, isTSpinMini, score);

        } else if (isPerfectClear) {
            return LineClearResult.perfectClear(linesCleared, clearedRows, false, score);

        } else {
            return LineClearResult.normal(linesCleared, clearedRows, score);
        }
    }

    private static boolean checkPerfectClear(GameState state) {
        for (int row = 0; row < state.getBoardHeight(); row++) {
            for (int col = 0; col < state.getBoardWidth(); col++) {
                if (state.getGrid()[row][col].isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 점수를 계산합니다
     * 
     * 테트리스의 점수 시스템은 매우 복잡합니다:
     * - 기본 점수: 라인 수에 따라 다름 (Single < Double < Triple < Tetris)
     * - T-Spin 보너스: T-Spin은 더 높은 점수
     * - Perfect Clear 보너스: 모든 블록을 지우면 추가 점수
     * - 콤보 보너스: 연속으로 라인을 지우면 추가 점수
     * - B2B 보너스: Tetris나 T-Spin을 연속으로 하면 1.5배
     * - 레벨 배수: 레벨이 높을수록 점수가 높음
     */
    private static long calculateScore(int lines, boolean tSpin, boolean tSpinMini,
                                       boolean perfectClear, int level, int combo, int b2b
    ) {
        long baseScore = 0;

        // 기본 점수 계산
        if (tSpin) {
            if(tSpinMini){
                baseScore = lines == 0 ? 100 : lines == 1 ? 200 : 400;
            } else {
                baseScore = lines == 0 ? 400 : lines == 1 ? 800 : lines == 2 ? 1200 : 1600;
            }
        } else {
            switch (lines) {
                case 1 : baseScore = 100; break;
                case 2 : baseScore = 300; break;
                case 3 : baseScore = 500; break;
                case 4 : baseScore = 800; break;
            }
        }

        // B2B 보너스 (1.5배)
        if (b2b > 0 && (lines == 4 || tSpin)) {
            baseScore = (long)(baseScore * 1.5);
        }

        // 콤보 보너스 (콤보 수 * 50)
        if (combo > 0) {
            baseScore += combo * 50 * level;
        }

        // 퍼펙트 클리어 보너스
        if (perfectClear) {
            baseScore += lines == 1 ? 800 : lines == 2 ? 1200 : lines == 3? 1800 : 2000;
        }

        // 레벨 배수
        return baseScore * level;
    }

    // ========== 위치 검증 헬퍼 메서드 ==========
    
    /**
     * 주어진 위치에 테트로미노를 놓을 수 있는지 검증합니다
     * 
     * 이 메서드는 GameEngine의 거의 모든 메서드에서 사용됩니다.
     * 이동하기 전, 회전하기 전 항상 검증이 필요하니까요.
     * 
     * @param state 현재 게임 상태
     * @param tetromino 검증할 테트로미노
     * @param x 검증할 X 위치
     * @param y 검증할 Y 위치
     * @return true면 놓을 수 있음, false면 충돌
     */
    private static boolean isValidPosition(GameState state, Tetromino tetromino, int x, int y){
        int[][] shape = tetromino.getCurrentShape();

        for(int row = 0; row < shape.length; row++){
            for(int col = 0; col < shape[0].length; col++){
                if(shape[row][col] == 1) {
                    int absX = x + (col - tetromino.getPivotX());
                    int absY = y + (row - tetromino.getPivotY());

                    // 보드 경계 체크
                    if(absX < 0 || absX >= state.getBoardWidth() || absY >= state.getBoardHeight()) {
                        return false;
                    }
                    // 다른 블록과 충돌 체크. (보드 위쪽은 통과 spawn 위치이므로 허용)
                    if(absY >= 0 && state.getGrid()[absY][absX].isOccupied()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
