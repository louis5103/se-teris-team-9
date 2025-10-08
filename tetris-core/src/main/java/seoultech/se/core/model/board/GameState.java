package seoultech.se.core.model.board;

import lombok.Data;
import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.block.enumType.TetrominoType;

@Data
public class GameState {
    // 보드 기본 정보
    private final int boardWidth;
    private final int boardHeight;
    private final Cell[][] grid;

    // 현재 테트로미노 정보
    private Tetromino currentTetromino;
    private int currentX;
    private int currentY;

    // Hold 기능 관련 정보
    private boolean holdUsedThisTurn;
    private TetrominoType heldPiece;

    // Next Queue (7-bag 시스템)
    private TetrominoType[] nextQueue;

    // 게임 통계 정보
    private long score;
    private int linesCleared;
    private int level;
    private boolean isGameOver;
    private String gameOverReason;

    // 콤보 및 백투백 정보
    private int comboCount;
    private boolean lastActionClearedLines; // 마지막 행동이 라인 클리어였는지 여부

    private int backToBackCount;
    private boolean lastClearWasDifficult; // Tetris 또는 T-spin이었는지 여부

    // Lock Delay 관련 정보
    private boolean isLockDelayActive;
    private int lockDelayResets;


    // 생성자
    // TODO: 빈보드 생성시에 필드 초기화 설정 여부 고려.
    public GameState(int width, int height) {
        this.boardWidth = width;
        this.boardHeight = height;
        this.grid = new Cell[height][width];

        // Cell 초기화
        for(int row = 0; row < height; row++) {
            for(int col = 0; col < width; col++) {
                grid[row][col] = Cell.empty();
            }
        }
        
        // Next Queue 초기화
        this.nextQueue = new TetrominoType[6]; // 6개를 미리 보기.

        // 초기 통계값.
        this.score = 0;
        this.level = 1;
        this.linesCleared = 0;
        this.isGameOver = false;
        
    }
    
    // 깊은 복사.
    public GameState deepCopy() {
        GameState copy = new GameState(boardWidth, boardHeight);

        // grid deep copy
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                copy.grid[row][col] = this.grid[row][col].copy();
            }
        }

        // 나머지 필드 복사
        copy.currentTetromino = this.currentTetromino != null ? this.currentTetromino.deepCopy() : null;
        copy.currentX = this.currentX;
        copy.currentY = this.currentY;
        copy.holdUsedThisTurn = this.holdUsedThisTurn;
        copy.heldPiece = this.heldPiece;
        copy.nextQueue = this.nextQueue != null ? this.nextQueue.clone() : null;
        copy.score = this.score;
        copy.linesCleared = this.linesCleared;
        copy.level = this.level;
        copy.isGameOver = this.isGameOver;
        copy.comboCount = this.comboCount;
        copy.backToBackCount = this.backToBackCount;
        copy.isLockDelayActive = this.isLockDelayActive;
        copy.lockDelayResets = this.lockDelayResets;
        return copy;
    }
}
