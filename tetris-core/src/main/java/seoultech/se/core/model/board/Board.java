package seoultech.se.core.model.board;

import lombok.Getter;
import lombok.Setter;
import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.block.enumType.RotationDirection;
import seoultech.se.core.model.block.enumType.RotationState;
import seoultech.se.core.model.block.enumType.TetrominoType;
import seoultech.se.core.model.board.enumType.WallKickEventData;

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

    public void spawnNewTetromino() {
        TetrominoType randomType = TetrominoType.getRandomTetrominoType();
        this.currentTetromino = new Tetromino(randomType);
        currentX = boardWidth / 2 - 1;
        currentY = 0;
    }

    private void rotate(RotationDirection direction) {
        TetrominoType currentType = currentTetromino.getType();
        RotationState fromRotationState = currentTetromino.getRotationState();

        if(currentType == TetrominoType.O) {
            return;
        }
        Tetromino rotatedTetromino = currentTetromino.getRotatedInstance(direction);
        RotationState toRotationState = rotatedTetromino.getRotationState();

        int[][] kickData = WallKickEventData.getKickData(currentType, fromRotationState, toRotationState);

        for(int[] offset : kickData) {
            int newX = currentX + offset[0];
            // TODO: y축 방향이 반대라 -로 변경되는게 맞는지 조사.
            int newY = currentY - offset[1];

            if(isValidPosition(rotatedTetromino, newX, newY)) {
                currentTetromino = rotatedTetromino;
                currentX = newX;
                currentY = newY;
                return;
            }
        }
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

                    if(absoluteX < 0 || absoluteX >= boardWidth || absoluteY < 0 || absoluteY >= boardHeight) {
                        return false;
                    }
                    if(absoluteY >= 0 && grid[absoluteY][absoluteX].isOccupied()) {
                        return false;
                    }
                    // TODO: 쌓인 테트로미노와 currentTetromino의 충돌검사
                    if(grid[absoluteY][absoluteX].isOccupied()) {
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
        } else {
            lockTetromino();
            spawnNewTetromino();
        }
    }
    public void moveLeft() {
        if(isValidPosition(currentTetromino, currentX - 1, currentY)) {
            currentX--;
        }
    }
    public void moveRight() {
        if(isValidPosition(currentTetromino, currentX + 1, currentY)) {
            currentX++;
        }
    }

    private void lockTetromino() {
        int[][] shape = currentTetromino.getCurrentShape();
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape.length; col++) {
                if(shape[row][col] == 1) {
                    int absoluteX = currentX + (col - currentTetromino.getPivotX());
                    int absoluteY = currentY + (row - currentTetromino.getPivotY());

                if (absoluteY < 0) {
                    this.gameState.setGameOver(true);
                    return;
                }

                    if(absoluteY >= 0 && absoluteY < boardHeight && absoluteX >= 0 && absoluteX < boardWidth) {
                        grid[absoluteY][absoluteX].setColor(currentTetromino.getColor());
                        grid[absoluteY][absoluteX].setOccupied(true);
                    }
                }
            }
        }
        clearLines();
    }

    private void clearLines() {
        int clearedRowCount = 0;
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
                for (int rowToPull = row; rowToPull > 0; rowToPull--) {
                    for(int col = 0; col < boardWidth; col++) {
                        grid[rowToPull][col] = grid[rowToPull - 1][col];
                    }
                }
                for(int col = 0; col < boardWidth; col++) {
                    grid[0][col] = Cell.empty();
                }
                row++;
            }
        }
        if(clearedRowCount > 0) {
            gameState.addLinesCleared(clearedRowCount);
            gameState.addScore(calculateScore(clearedRowCount));
        }
    }
    private long calculateScore(int clearedLines) {
        int level = gameState.getLevel();
        switch (clearedLines) {
            case 1: return 100 * level;
            case 2: return 300 * level;
            case 3: return 500 * level;
            case 4: return 800 * level;
            default: return 0;
        }
    }
}
