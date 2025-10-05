package seoultech.se.client.controller;

import org.springframework.stereotype.Component;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import seoultech.se.core.BoardObserver;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.command.implement.moveCommand.*; 
import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.block.enumType.RotationDirection;
import seoultech.se.core.model.block.enumType.TetrominoType;
import seoultech.se.core.model.board.Board;
import seoultech.se.core.model.board.Cell;
import seoultech.se.core.model.board.GameState;

/**
 * 테트리스 게임 컨트롤러
 *
 * BoardObserver의 모든 메서드를 구현합니다.
 * 당장 사용하지 않는 메서드는 로그만 찍거나 비워두었습니다.
 * 나중에 필요한 기능을 구현할 때 해당 메서드를 채우면 됩니다.
 */
@Component
public class GameController implements BoardObserver {

    @FXML private GridPane boardGridPane;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label linesLabel;
    @FXML private Label gameOverLabel;

    private Board board;
    private Rectangle[][] cellRectangles;
    private AnimationTimer gameLoop;
    private long lastUpdateTime = 0;
    private long dropInterval = 500_000_000L;

    private static final double CELL_SIZE = 30.0;

    @FXML
    public void initialize() {
        System.out.println("🎮 GameController initializing...");

        board = new Board();
        board.addObserver(this);  // 이 Controller를 Observer로 등록

        System.out.println("📊 Board created: " + board.getBoardWidth() + "x" + board.getBoardHeight());

        initializeGridPane();
        updateGameInfoLabels();
        setupGameLoop();
        setupKeyboardControls();

        board.spawnNewTetromino();
        startGame();

        System.out.println("✅ GameController initialization complete!");
    }

    private void initializeGridPane() {
        int width = board.getBoardWidth();
        int height = board.getBoardHeight();

        System.out.println("🎨 Initializing GridPane with " + width + "x" + height + " cells...");

        cellRectangles = new Rectangle[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);

                // ⭐ 중요: 기본 색상을 명시적으로 설정
                // 이렇게 하면 CSS가 로드되지 않아도 최소한 윤곽선은 보입니다
                rect.setFill(Color.rgb(26, 26, 26));  // 어두운 회색 (빈 셀)
                rect.setStroke(Color.rgb(51, 51, 51));  // 약간 밝은 회색 (테두리)
                rect.setStrokeWidth(0.5);

                // CSS 스타일 클래스 추가
                rect.getStyleClass().add("board-cell");

                // GridPane에 추가
                boardGridPane.add(rect, col, row);
                cellRectangles[row][col] = rect;
            }
        }

        System.out.println("✅ GridPane initialized with " + (width * height) + " cells");
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (board.getGameState().isGameOver()) {
                    stop();
                    return;
                }

                if (now - lastUpdateTime >= dropInterval) {
                    GameCommand moveDownCommand = new MoveDownCommand();
                    moveDownCommand.execute(board);
                    lastUpdateTime = now;
                }
            }
        };
    }

    private void setupKeyboardControls() {
        boardGridPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
                System.out.println("⌨️  Keyboard controls enabled");
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (board.getGameState().isGameOver()) {
            return;
        }

        GameCommand command = null;

        switch (event.getCode()) {
            case LEFT:
                command = new MoveLeftCommand();
                break;
            case RIGHT:
                command = new MoveRightCommand();
                break;
            case DOWN:
                command = new MoveDownCommand();
                break;
            case UP:
                command = new RotateClockwiseCommand();
                break;
            case Z:
                command = new RotateCounterClockwiseCommand();
                break;
            case SPACE:
                command = new HardDropCommand();
                break;
            case C:
                command = new HoldCommand();
                break;
        }

        if (command != null) {
            command.execute(board);
            event.consume();
        }
    }

    // ========== BoardObserver 구현 - 기본 셀/보드 변경 ==========

    @Override
    public void onCellChanged(int row, int col, Cell cell) {
        Platform.runLater(() -> {
            updateCellRectangle(row, col, cell);
        });
    }

    @Override
    public void onMultipleCellsChanged(int[] rows, int[] cols, Cell[][] cells) {
        // TODO: 구현하면 성능 향상 가능
    }

    // ========== BoardObserver 구현 - 테트로미노 이동/회전 ==========

    @Override
    public void onTetrominoMoved(int x, int y, Tetromino tetromino) {
        Platform.runLater(() -> {
            drawCurrentTetromino();
        });
    }

    @Override
    public void onTetrominoRotated(RotationDirection direction, int kickIndex) {
        System.out.println("🔄 Rotated " + direction + " (kick index: " + kickIndex + ")");
    }

    @Override
    public void onTetrominoRotationFailed(RotationDirection direction) {
        System.out.println("❌ Rotation failed: " + direction);
    }

    @Override
    public void onTetrominoLocked(Tetromino tetromino) {
        System.out.println("🔒 Tetromino locked: " + tetromino.getType());
    }

    @Override
    public void onTetrominoLockDelayStarted() {
        // TODO: Lock Delay 타이머 UI 표시
    }

    @Override
    public void onTetrominoLockDelayReset(int remainingResets) {
        // TODO: 남은 리셋 횟수 표시
    }

    // ========== BoardObserver 구현 - 테트로미노 생성 ==========

    @Override
    public void onTetrominoSpawned(Tetromino tetromino) {
        Platform.runLater(() -> {
            drawCurrentTetromino();
        });
        System.out.println("🎲 New tetromino spawned: " + tetromino.getType());
    }

    @Override
    public void onNextQueueUpdated(TetrominoType[] nextPieces) {
        System.out.println("📋 Next queue updated: " + java.util.Arrays.toString(nextPieces));
    }

    // ========== BoardObserver 구현 - Hold 시스템 ==========

    @Override
    public void onHoldChanged(TetrominoType heldPiece, TetrominoType previousPiece) {
        System.out.println("💾 Hold changed: " + heldPiece);
    }

    @Override
    public void onHoldFailed() {
        System.out.println("⚠️ Hold failed (already used this turn)");
    }

    // ========== BoardObserver 구현 - 라인 클리어 ==========

    @Override
    public void onLineCleared(int linesCleared, int[] clearedRows,
                              boolean isTSpin, boolean isTSpinMini, boolean isPerfectClear) {
        String clearType = isTSpin ? "T-SPIN " : "";
        if (isTSpinMini) clearType += "MINI ";

        System.out.println("✨ Line cleared: " + clearType + linesCleared + " lines");

        if (isPerfectClear) {
            System.out.println("🌟 PERFECT CLEAR!");
        }
    }

    @Override
    public void onCombo(int comboCount) {
        System.out.println("🔥 COMBO x" + comboCount);
    }

    @Override
    public void onComboBreak(int finalComboCount) {
        System.out.println("💨 Combo ended: " + finalComboCount);
    }

    @Override
    public void onBackToBack(int backToBackCount) {
        System.out.println("⚡ BACK-TO-BACK x" + backToBackCount);
    }

    @Override
    public void onBackToBackBreak(int finalBackToBackCount) {
        System.out.println("💨 B2B ended: " + finalBackToBackCount);
    }

    // ========== BoardObserver 구현 - 점수 및 게임 상태 ==========

    @Override
    public void onScoreAdded(long points, String reason) {
        System.out.println("💰 +" + points + " points (" + reason + ")");
    }

    @Override
    public void onGameStateChanged(GameState gameState) {
        Platform.runLater(() -> {
            updateGameInfoLabels();

            // 레벨에 따라 낙하 속도 조정
            dropInterval = Math.max(100_000_000L,
                                   500_000_000L - (gameState.getLevel() * 50_000_000L));
        });
    }

    @Override
    public void onLevelUp(int newLevel) {
        System.out.println("📈 LEVEL UP! Now at level " + newLevel);
    }

    // ========== BoardObserver 구현 - 게임 진행 ==========

    @Override
    public void onGamePaused() {
        System.out.println("⏸️ Game paused");
    }

    @Override
    public void onGameResumed() {
        System.out.println("▶️ Game resumed");
    }

    @Override
    public void onGameOver(String reason) {
        Platform.runLater(() -> {
            gameOverLabel.setVisible(true);
            System.out.println("💀 GAME OVER (" + reason + ")");
            System.out.println("   Final Score: " + board.getGameState().getScore());
            System.out.println("   Lines Cleared: " + board.getGameState().getLinesCleared());
        });
    }

    // ========== BoardObserver 구현 - 멀티플레이어 ==========

    @Override
    public void onGarbageLinesAdded(int lines, String sourcePlayerId) {
        System.out.println("💥 Received " + lines + " garbage lines from " + sourcePlayerId);
    }

    @Override
    public void onGarbageLinesCleared(int lines) {
        System.out.println("🛡️ Cleared " + lines + " incoming garbage lines");
    }

    @Override
    public void onAttackSent(String targetPlayerId, int lines) {
        System.out.println("⚔️ Sent " + lines + " lines to " + targetPlayerId);
    }

    // ========== BoardObserver 구현 - 디버그 ==========

    @Override
    public void onDebugInfoUpdated(String debugInfo) {
        if (System.getProperty("debug.mode") != null) {
            System.out.println("🐛 " + debugInfo);
        }
    }

    // ========== UI 업데이트 헬퍼 메서드들 ==========

    private void updateCellRectangle(int row, int col, Cell cell) {
        Rectangle rect = cellRectangles[row][col];

        if (cell.isOccupied()) {
            // 셀이 차있으면 색상 클래스 추가
            String colorClass = getCssColorClass(cell.getColor());
            clearCellColor(rect);
            if (colorClass != null) {
                rect.getStyleClass().add(colorClass);
            }

            // ⭐ JavaFX Color로도 직접 설정 (CSS가 없어도 보이도록)
            rect.setFill(getJavaFXColor(cell.getColor()));

        } else {
            // 빈 셀이면 기본 색상으로 되돌림
            clearCellColor(rect);
            rect.setFill(Color.rgb(26, 26, 26));
        }
    }

    private void drawCurrentTetromino() {
        // 전체 보드를 다시 그려서 이전 테트로미노 위치 지우기
        Cell[][] grid = board.getGrid();
        for (int row = 0; row < board.getBoardHeight(); row++) {
            for (int col = 0; col < board.getBoardWidth(); col++) {
                updateCellRectangle(row, col, grid[row][col]);
            }
        }

        if (board.getCurrentTetromino() == null) {
            return;
        }

        int[][] shape = board.getCurrentTetromino().getCurrentShape();
        int pivotX = board.getCurrentTetromino().getPivotX();
        int pivotY = board.getCurrentTetromino().getPivotY();

        seoultech.se.core.model.block.enumType.Color tetrominoColor =
            board.getCurrentTetromino().getColor();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[0].length; col++) {
                if (shape[row][col] == 1) {
                    int absoluteX = board.getCurrentX() + (col - pivotX);
                    int absoluteY = board.getCurrentY() + (row - pivotY);

                    if (absoluteY >= 0 && absoluteY < board.getBoardHeight() &&
                        absoluteX >= 0 && absoluteX < board.getBoardWidth()) {

                        Rectangle rect = cellRectangles[absoluteY][absoluteX];
                        String colorClass = getCssColorClass(tetrominoColor);
                        clearCellColor(rect);
                        if (colorClass != null) {
                            rect.getStyleClass().add(colorClass);
                        }

                        // ⭐ JavaFX Color로도 직접 설정
                        rect.setFill(getJavaFXColor(tetrominoColor));
                    }
                }
            }
        }
    }

    private void updateGameInfoLabels() {
        GameState state = board.getGameState();
        scoreLabel.setText(String.valueOf(state.getScore()));
        levelLabel.setText(String.valueOf(state.getLevel()));
        linesLabel.setText(String.valueOf(state.getLinesCleared()));
    }

    private void clearCellColor(Rectangle rect) {
        rect.getStyleClass().removeAll(
            "tetromino-red", "tetromino-green", "tetromino-blue",
            "tetromino-yellow", "tetromino-cyan", "tetromino-magenta",
            "tetromino-orange"
        );
    }

    private String getCssColorClass(seoultech.se.core.model.block.enumType.Color color) {
        switch (color) {
            case RED:     return "tetromino-red";
            case GREEN:   return "tetromino-green";
            case BLUE:    return "tetromino-blue";
            case YELLOW:  return "tetromino-yellow";
            case CYAN:    return "tetromino-cyan";
            case MAGENTA: return "tetromino-magenta";
            case ORANGE:  return "tetromino-orange";
            default:      return null;
        }
    }

    /**
     * ⭐ 새로 추가: Core 모듈의 Color를 JavaFX Color로 변환
     * CSS가 없어도 블록이 보이도록 하는 안전장치
     */
    private Color getJavaFXColor(seoultech.se.core.model.block.enumType.Color color) {
        switch (color) {
            case RED:     return Color.rgb(255, 68, 68);     // 밝은 빨강
            case GREEN:   return Color.rgb(68, 255, 68);     // 밝은 초록
            case BLUE:    return Color.rgb(68, 68, 255);     // 밝은 파랑
            case YELLOW:  return Color.rgb(255, 255, 68);    // 밝은 노랑
            case CYAN:    return Color.rgb(68, 255, 255);    // 밝은 청록
            case MAGENTA: return Color.rgb(255, 68, 255);    // 밝은 마젠타
            case ORANGE:  return Color.rgb(255, 136, 68);    // 밝은 주황
            default:      return Color.rgb(128, 128, 128);   // 회색 (기본값)
        }
    }

    // ========== 게임 제어 ==========

    public void startGame() {
        gameOverLabel.setVisible(false);
        lastUpdateTime = System.nanoTime();
        gameLoop.start();
        boardGridPane.requestFocus();
        System.out.println("🎮 Game Started!");
    }

    public void pauseGame() {
        gameLoop.stop();
    }

    public void resumeGame() {
        lastUpdateTime = System.nanoTime();
        gameLoop.start();
    }
}
