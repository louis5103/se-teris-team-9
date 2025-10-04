package seoultech.se.core.model.board;

import lombok.Getter;
import lombok.Setter;
import seoultech.se.core.BoardObserver;
import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.block.enumType.RotationDirection;
import seoultech.se.core.model.block.enumType.RotationState;
import seoultech.se.core.model.block.enumType.TetrominoType;
import seoultech.se.core.model.board.enumType.WallKickEventData;

import java.util.ArrayList;
import java.util.List;

/**
 * 게임 보드 (Observer 패턴 통합 + 오프라인 싱글 플레이 완전 지원)
 * 
 * 현재 구현된 기능:
 * - 기본 이동/회전 (SRS Wall Kick 포함)
 * - 라인 클리어
 * - 점수 계산
 * - Observer 알림 시스템
 * - Hard Drop
 * 
 * TODO: 향후 구현 예정
 * - Hold 시스템
 * - 7-bag 랜덤 시스템
 * - T-Spin 감지
 * - 콤보/B2B 시스템
 * - Lock Delay
 */
@Getter
@Setter
public class Board {
    private static final int BOARD_WIDTH_DEFAULT = 10;
    private static final int BOARD_HEIGHT_DEFAULT = 20;

    private final Cell[][] grid;
    private final int boardWidth;
    private final int boardHeight;

    private Tetromino currentTetromino;
    private int currentX;
    private int currentY;

    private final GameState gameState;
    
    // Observer 패턴 - 상태 변경 알림을 위한 리스너 목록
    private final List<BoardObserver> observers = new ArrayList<>();

    public Board() {
        this.boardWidth = BOARD_WIDTH_DEFAULT;
        this.boardHeight = BOARD_HEIGHT_DEFAULT;
        this.gameState = new GameState();

        grid = new Cell[boardHeight][boardWidth];
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                grid[row][col] = Cell.empty();
            }
        }
    }
    
    // ========== Observer 관리 메서드 ==========
    
    /**
     * Observer를 등록합니다.
     * 등록된 Observer는 Board의 모든 변경사항을 통지받습니다.
     */
    public void addObserver(BoardObserver observer) {
        observers.add(observer);
    }
    
    /**
     * Observer를 제거합니다.
     */
    public void removeObserver(BoardObserver observer) {
        observers.remove(observer);
    }
    
    // ========== 게임 로직 (Observer 알림 통합) ==========

    public void spawnNewTetromino() {
        // TODO: 7-bag 시스템으로 변경 필요
        TetrominoType randomType = TetrominoType.getRandomTetrominoType();
        this.currentTetromino = new Tetromino(randomType);
        currentX = boardWidth / 2 - 1;
        currentY = 0;
        
        // Observer에게 알림
        notifyTetrominoSpawned(currentTetromino);
        notifyTetrominoMoved(currentX, currentY, currentTetromino);
    }

    private void rotate(RotationDirection direction) {
        TetrominoType currentType = currentTetromino.getType();
        RotationState fromRotationState = currentTetromino.getRotationState();

        if(currentType == TetrominoType.O) {
            return;  // O 블록은 회전하지 않음
        }
        
        Tetromino rotatedTetromino = currentTetromino.getRotatedInstance(direction);
        RotationState toRotationState = rotatedTetromino.getRotationState();

        int[][] kickData = WallKickEventData.getKickData(currentType, fromRotationState, toRotationState);

        // Wall Kick 시스템: 5가지 위치를 순서대로 시도
        for(int kickIndex = 0; kickIndex < kickData.length; kickIndex++) {
            int[] offset = kickData[kickIndex];
            int newX = currentX + offset[0];
            int newY = currentY - offset[1];

            if(isValidPosition(rotatedTetromino, newX, newY)) {
                // 회전 성공
                currentTetromino = rotatedTetromino;
                currentX = newX;
                currentY = newY;
                
                // Observer에게 회전 성공 알림
                notifyTetrominoRotated(direction, kickIndex);
                notifyTetrominoMoved(currentX, currentY, currentTetromino);
                return;
            }
        }
        
        // 모든 Wall Kick 시도 실패
        notifyTetrominoRotationFailed(direction);
    }

    public void rotateClockwise() {
        rotate(RotationDirection.CLOCKWISE);
    }
    
    public void rotateCounterClockwise() {
        rotate(RotationDirection.COUNTER_CLOCKWISE);
    }

    private boolean isValidPosition(Tetromino tetromino, int newX, int newY) {
        int[][] shape = tetromino.getCurrentShape();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape.length; col++) {
                if(shape[row][col] == 1) {
                    int absoluteX = newX + (col - tetromino.getPivotX());
                    int absoluteY = newY + (row - tetromino.getPivotY());

                    // 보드 경계 체크
                    if(absoluteX < 0 || absoluteX >= boardWidth || absoluteY < 0 || absoluteY >= boardHeight) {
                        return false;
                    }
                    
                    // 다른 블록과 충돌 체크
                    if(absoluteY >= 0 && grid[absoluteY][absoluteX].isOccupied()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void moveDown() {
        if(isValidPosition(currentTetromino, currentX, currentY + 1)) {
            currentY++;
            notifyTetrominoMoved(currentX, currentY, currentTetromino);
        } else {
            // 더 이상 내려갈 수 없으면 고정하고 새 블록 생성
            lockTetromino();
            spawnNewTetromino();
        }
    }
    
    public void moveLeft() {
        if(isValidPosition(currentTetromino, currentX - 1, currentY)) {
            currentX--;
            notifyTetrominoMoved(currentX, currentY, currentTetromino);
        }
    }
    
    public void moveRight() {
        if(isValidPosition(currentTetromino, currentX + 1, currentY)) {
            currentX++;
            notifyTetrominoMoved(currentX, currentY, currentTetromino);
        }
    }
    
    /**
     * Hard Drop: 블록을 즉시 바닥까지 떨어뜨리고 고정합니다.
     */
    public void hardDrop() {
        // 바닥까지 이동
        while(isValidPosition(currentTetromino, currentX, currentY + 1)) {
            currentY++;
        }
        notifyTetrominoMoved(currentX, currentY, currentTetromino);
        
        // 즉시 고정
        lockTetromino();
        spawnNewTetromino();
    }
    
    /**
     * Hold 기능 (TODO: 구현 필요)
     */
    public void hold() {
        // TODO: Hold 시스템 구현
        System.out.println("⚠️ Hold not implemented yet");
    }

    private void lockTetromino() {
        int[][] shape = currentTetromino.getCurrentShape();
        
        // 블록을 보드에 고정
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape.length; col++) {
                if(shape[row][col] == 1) {
                    int absoluteX = currentX + (col - currentTetromino.getPivotX());
                    int absoluteY = currentY + (row - currentTetromino.getPivotY());

                    // 게임 오버 체크: 블록이 spawn 위치를 벗어남
                    if (absoluteY < 0) {
                        this.gameState.setGameOver(true);
                        notifyGameOver("BLOCK_OUT");
                        return;
                    }

                    // 셀에 블록 색상 설정
                    if(absoluteY >= 0 && absoluteY < boardHeight && absoluteX >= 0 && absoluteX < boardWidth) {
                        grid[absoluteY][absoluteX].setColor(currentTetromino.getColor());
                        grid[absoluteY][absoluteX].setOccupied(true);
                        
                        // 각 셀 변경 알림
                        notifyCellChanged(absoluteY, absoluteX, grid[absoluteY][absoluteX]);
                    }
                }
            }
        }
        
        // 테트로미노 고정 알림
        notifyTetrominoLocked(currentTetromino);
        
        // 라인 클리어 체크
        clearLines();
    }

    private void clearLines() {
        int clearedRowCount = 0;
        List<Integer> clearedRowsList = new ArrayList<>();
        
        // 아래에서 위로 라인 체크
        for (int row = boardHeight - 1; row >= 0; row--) {
            boolean isFullLine = true;
            for (int col = 0; col < boardWidth; col++) {
                if (!grid[row][col].isOccupied()) {
                    isFullLine = false;
                    break;
                }
            }
            
            if (isFullLine) {
                clearedRowCount++;
                clearedRowsList.add(row);
                
                // 라인을 아래로 당기기
                for (int rowToPull = row; rowToPull > 0; rowToPull--) {
                    for(int col = 0; col < boardWidth; col++) {
                        grid[rowToPull][col] = grid[rowToPull - 1][col];
                        notifyCellChanged(rowToPull, col, grid[rowToPull][col]);
                    }
                }
                
                // 맨 위 줄 비우기
                for(int col = 0; col < boardWidth; col++) {
                    grid[0][col] = Cell.empty();
                    notifyCellChanged(0, col, grid[0][col]);
                }
                
                row++;  // 같은 행을 다시 체크 (아래로 당겨진 새 줄)
            }
        }
        
        // 라인을 지웠다면 점수 계산 및 알림
        if(clearedRowCount > 0) {
            gameState.addLinesCleared(clearedRowCount);
            long points = calculateScore(clearedRowCount);
            gameState.addScore(points);
            
            // TODO: T-Spin, Perfect Clear 감지 구현 필요
            boolean isTSpin = false;
            boolean isTSpinMini = false;
            boolean isPerfectClear = checkPerfectClear();
            
            int[] clearedRows = clearedRowsList.stream().mapToInt(i -> i).toArray();
            
            // Observer들에게 알림
            notifyLineCleared(clearedRowCount, clearedRows, isTSpin, isTSpinMini, isPerfectClear);
            
            String reason = getScoreReason(clearedRowCount, isTSpin, isTSpinMini, isPerfectClear);
            notifyScoreAdded(points, reason);
            notifyGameStateChanged(gameState);
            
            // TODO: 콤보/B2B 시스템 구현 필요
        }
    }
    
    /**
     * Perfect Clear (보드의 모든 블록이 사라짐) 체크
     */
    private boolean checkPerfectClear() {
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                if (grid[row][col].isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private long calculateScore(int clearedLines) {
        int level = gameState.getLevel();
        switch (clearedLines) {
            case 1: return 100 * level;   // Single
            case 2: return 300 * level;   // Double
            case 3: return 500 * level;   // Triple
            case 4: return 800 * level;   // Tetris
            default: return 0;
        }
    }
    
    private String getScoreReason(int lines, boolean isTSpin, boolean isTSpinMini, boolean isPerfectClear) {
        if (isPerfectClear) {
            return "PERFECT_CLEAR";
        }
        if (isTSpin) {
            switch(lines) {
                case 1: return "T-SPIN_SINGLE";
                case 2: return "T-SPIN_DOUBLE";
                case 3: return "T-SPIN_TRIPLE";
            }
        }
        switch(lines) {
            case 1: return "SINGLE";
            case 2: return "DOUBLE";
            case 3: return "TRIPLE";
            case 4: return "TETRIS";
            default: return "UNKNOWN";
        }
    }
    
    // ========== Observer 알림 메서드들 ==========
    
    private void notifyCellChanged(int row, int col, Cell cell) {
        for (BoardObserver observer : observers) {
            observer.onCellChanged(row, col, cell);
        }
    }
    
    private void notifyTetrominoMoved(int x, int y, Tetromino tetromino) {
        for (BoardObserver observer : observers) {
            observer.onTetrominoMoved(x, y, tetromino);
        }
    }
    
    private void notifyTetrominoRotated(RotationDirection direction, int kickIndex) {
        for (BoardObserver observer : observers) {
            observer.onTetrominoRotated(direction, kickIndex);
        }
    }
    
    private void notifyTetrominoRotationFailed(RotationDirection direction) {
        for (BoardObserver observer : observers) {
            observer.onTetrominoRotationFailed(direction);
        }
    }
    
    private void notifyTetrominoLocked(Tetromino tetromino) {
        for (BoardObserver observer : observers) {
            observer.onTetrominoLocked(tetromino);
        }
    }
    
    private void notifyTetrominoSpawned(Tetromino tetromino) {
        for (BoardObserver observer : observers) {
            observer.onTetrominoSpawned(tetromino);
        }
    }
    
    private void notifyLineCleared(int linesCleared, int[] clearedRows, 
                                   boolean isTSpin, boolean isTSpinMini, boolean isPerfectClear) {
        for (BoardObserver observer : observers) {
            observer.onLineCleared(linesCleared, clearedRows, isTSpin, isTSpinMini, isPerfectClear);
        }
    }
    
    private void notifyScoreAdded(long points, String reason) {
        for (BoardObserver observer : observers) {
            observer.onScoreAdded(points, reason);
        }
    }
    
    private void notifyGameStateChanged(GameState gameState) {
        for (BoardObserver observer : observers) {
            observer.onGameStateChanged(gameState);
        }
    }
    
    private void notifyGameOver(String reason) {
        for (BoardObserver observer : observers) {
            observer.onGameOver(reason);
        }
    }
}
