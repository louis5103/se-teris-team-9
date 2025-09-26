package seoultech.se.core.model.board;

import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.block.enumType.TetrominoType;

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
}
