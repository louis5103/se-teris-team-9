package seoultech.se.core;

import lombok.Data;
import seoultech.se.core.model.Cell;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.TetrominoType;

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
    
    // 게임 상태
    private boolean isPaused;
    
    // T-Spin 감지를 위한 정보
    private boolean lastActionWasRotation;  // 마지막 액션이 회전이었는지


    // 생성자
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

        // 콤보/B2B 초기화
        this.comboCount = 0;
        this.backToBackCount = 0;
        this.lastActionClearedLines = false;
        this.lastClearWasDifficult = false;

        // Hold 초기화
        this.heldPiece = null;
        this.holdUsedThisTurn = false;

        // Lock Delay 초기화
        this.isLockDelayActive = false;
        this.lockDelayResets = 0;
        
        // 게임 상태 초기화
        this.isPaused = false;
        
        // T-Spin 감지 초기화
        this.lastActionWasRotation = false;
    }
    
    // 깊은 복사.
    public GameState deepCopy() {
        GameState copy = new GameState(boardWidth, boardHeight);

        // grid 깊은 복사 - 각 셀 초기화.
        for (int row = 0; row < boardHeight; row++) {
            for (int col = 0; col < boardWidth; col++) {
                copy.grid[row][col] = this.grid[row][col].copy();
            }
        }

        // 현재 테트로미노 복사
        copy.currentTetromino = this.currentTetromino != null ? this.currentTetromino : null;
        copy.currentX = this.currentX;
        copy.currentY = this.currentY;

        // Hold 기능 관련 정보 복사
        copy.holdUsedThisTurn = this.holdUsedThisTurn;
        copy.heldPiece = this.heldPiece;

        // Next Queue 복사
        if(this.nextQueue != null) {
            copy.nextQueue = this.nextQueue.clone();
        }

        // 통계 정보 복사
        copy.score = this.score;
        copy.linesCleared = this.linesCleared;
        copy.level = this.level;
        copy.isGameOver = this.isGameOver;
        copy.gameOverReason = this.gameOverReason;

        // 콤보/B2B 복사
        copy.comboCount = this.comboCount;
        copy.lastActionClearedLines = this.lastActionClearedLines;

        copy.backToBackCount = this.backToBackCount;
        copy.lastClearWasDifficult = this.lastClearWasDifficult;
        
        // Lock Delay 복사
        copy.isLockDelayActive = this.isLockDelayActive;
        copy.lockDelayResets = this.lockDelayResets;
        
        // T-Spin 관련 복사
        copy.lastActionWasRotation = this.lastActionWasRotation;
        
        return copy;
    }

    public void addScore(long points) {
        this.score += points;
    }

    public void addLinesCleared(int count) {
        this.linesCleared += count;
        this.level = (this.linesCleared / 10) + 1; // 예: 10라인마다 레벨업
    }
}
