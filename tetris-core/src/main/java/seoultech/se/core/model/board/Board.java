package seoultech.se.core.model.board;

import lombok.Getter;
import lombok.Setter;
import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.block.enumType.TetrominoType;

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

    private long score = 0;
    private int linesCleared = 0;
    private int level = 1;

    public Board() {
        this.boardWidth = BOARD_WIDTH_DEFAULT;
        this.boardHeight = BOARD_HEIGHT_DEFAULT;

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

    public void rotate() {
        Tetromino rotatedTetromino = currentTetromino.getRotatedInstance();

        // TODO: 월킥 테스트. SRS 데이터 테이블로 교체.
        int[][] offsets = {{0, 0}, {1, 0}, {-1, 0}, {0, -1}, {-2, 0}, {2, 0}, {0, -2}};
        for (int[] offset : offsets) {
            if(isValidPosition(rotatedTetromino, currentX + offset[0], currentY + offset[1])) {
                currentTetromino = rotatedTetromino;
                currentX += offset[0];
                currentY += offset[1];
                return;
            }
        }
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

                    if(absoluteY >= 0 && absoluteY < boardHeight && absoluteX >= 0 && absoluteX < boardWidth) {
                        grid[absoluteY][absoluteX].setColor(currentTetromino.getColor());
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
            linesCleared += clearedRowCount;
            score += calculateScore(clearedRowCount);
            level = linesCleared / 10 + 1;
        }
    }
    private long calculateScore(int clearedLines) {
        switch (clearedLines) {
            case 1: return 100 * level;
            case 2: return 300 * level;
            case 3: return 500 * level;
            case 4: return 800 * level;
            default: return 0;
        }
    }
}
