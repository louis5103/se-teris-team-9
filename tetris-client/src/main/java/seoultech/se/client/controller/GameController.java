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
import seoultech.se.core.GameState;
import seoultech.se.core.model.BoardObserver;
import seoultech.se.core.model.Cell;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * JavaFX UI를 제어하는 컨트롤러
 * 역할:
    * - 사용자 입력을 받아서 BoardController에 전달
    * - BoardObserver로서 게임 이벤트를 받아서 UI 업데이트
    * - 게임 루프(AnimationTimer) 관리
 * 
 * 게임 로직이나 상태 관리는 하지 않음.
    * BoardController와 GameEngine 몫.
 */
@Component
public class GameController implements BoardObserver {

    @FXML private GridPane boardGridPane;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label linesLabel;
    @FXML private Label gameOverLabel;

    private BoardController boardController;
    private Rectangle[][] cellRectangles;
    private AnimationTimer gameLoop;
    private long lastUpdateTime = 0;
    private long dropInterval = 500_000_000L; // 0.5초 (나노초 단위)

    private static final double CELL_SIZE = 30.0;

    /**
     * FXML이 로드된 후 자동으로 호출.
     * 
     * 초기화 순서:
     * 1. BoardController 생성
     * 2. 이 Controller를 Observer로 등록
     * 3. UI 초기화
     * 4. 게임 루프 설정
     * 5. 게임 시작
     */
    @FXML
    public void initialize() {
        System.out.println("🎮 GameController initializing...");

        // BoardController 생성 및 Observer 등록
        boardController = new BoardController();
        boardController.addObserver(this);

        GameState gameState = boardController.getGameState();
        System.out.println("📊 Board created: " + gameState.getBoardWidth() + "x" + gameState.getBoardHeight());

        initializeGridPane(gameState);
        updateGameInfoLabels();
        setupGameLoop();
        setupKeyboardControls();
        startGame();

        System.out.println("✅ GameController initialization complete!");
    }

    /**
     * GridPane을 초기화하고 모든 셀의 Rectangle을 생성합니다
     */
    private void initializeGridPane(GameState gameState) {
        int width = gameState.getBoardWidth();
        int height = gameState.getBoardHeight();

        System.out.println("🎨 Initializing GridPane with " + width + "x" + height + " cells...");

        cellRectangles = new Rectangle[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);

                // 기본 색상 설정 (CSS가 없어도 보이도록)
                rect.setFill(Color.rgb(26, 26, 26));
                rect.setStroke(Color.rgb(51, 51, 51));
                rect.setStrokeWidth(0.5);

                // CSS 클래스 추가
                rect.getStyleClass().add("board-cell");

                // GridPane에 추가
                boardGridPane.add(rect, col, row);
                cellRectangles[row][col] = rect;
            }
        }

        System.out.println("✅ GridPane initialized with " + (width * height) + " cells");
    }

    /**
     * 게임 루프를 설정합니다
     * 
     * 이 루프는 일정 시간마다 블록을 한 칸 아래로 내립니다.
     * 레벨이 올라가면 dropInterval이 짧아져서 속도가 빨라집니다.
     */
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                GameState gameState = boardController.getGameState();
                
                if (gameState.isGameOver()) {
                    stop();
                    return;
                }

                if (now - lastUpdateTime >= dropInterval) {
                    boardController.moveDown();
                    lastUpdateTime = now;
                }
            }
        };
    }

    /**
     * 키보드 입력을 처리합니다
     * 
     * 키 입력을 BoardController의 메서드 호출로 변환합니다.
     * 이것이 입력 레이어와 도메인 레이어 사이의 번역입니다.
     */
    private void setupKeyboardControls() {
        boardGridPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
                System.out.println("⌨️  Keyboard controls enabled");
            }
        });
    }

    private void handleKeyPress(KeyEvent event) {
        if (boardController.getGameState().isGameOver()) {
            return;
        }

        switch (event.getCode()) {
            case LEFT:
                boardController.moveLeft();
                break;
            case RIGHT:
                boardController.moveRight();
                break;
            case DOWN:
                boardController.moveDown();
                break;
            case UP:
                boardController.rotateClockwise();
                break;
            case Z:
                boardController.rotateCounterClockwise();
                break;
            case SPACE:
                boardController.hardDrop();
                break;
            case C:
                boardController.hold();
                break;
        }

        event.consume();
    }

    // ========== BoardObserver 구현 ==========
    // 이 메서드들은 BoardController로부터 이벤트를 받습니다
    // 각 이벤트를 JavaFX UI 업데이트로 변환합니다

    @Override
    public void onCellChanged(int row, int col, Cell cell) {
        Platform.runLater(() -> {
            updateCellRectangle(row, col, cell);
        });
    }

    @Override
    public void onMultipleCellsChanged(int[] rows, int[] cols, Cell[][] cells) {
        // TODO: 나중에 성능 최적화를 위해 구현
    }

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
        // TODO: Lock Delay UI
    }

    @Override
    public void onTetrominoLockDelayReset(int remainingResets) {
        // TODO: Lock Delay UI
    }

    @Override
    public void onTetrominoSpawned(Tetromino tetromino) {
        System.out.println("🎲 New tetromino spawned: " + tetromino.getType());
    }

    @Override
    public void onNextQueueUpdated(TetrominoType[] nextPieces) {
        // TODO: Next Queue UI
        System.out.println("📋 Next queue updated");
    }

    @Override
    public void onHoldChanged(TetrominoType heldPiece, TetrominoType previousPiece) {
        System.out.println("💾 Hold changed: " + heldPiece);
    }

    @Override
    public void onHoldFailed() {
        System.out.println("⚠️ Hold failed (already used this turn)");
    }

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

    @Override
    public void onScoreAdded(long points, String reason) {
        System.out.println("💰 +" + points + " points (" + reason + ")");
    }

    @Override
    public void onGameStateChanged(GameState gameState) {
        Platform.runLater(() -> {
            updateGameInfoLabels();

            // 레벨에 따라 낙하 속도 조정
            // 레벨이 높을수록 빠르게 떨어집니다
            dropInterval = Math.max(100_000_000L,
                                   500_000_000L - (gameState.getLevel() * 50_000_000L));
        });
    }

    @Override
    public void onLevelUp(int newLevel) {
        System.out.println("📈 LEVEL UP! Now at level " + newLevel);
    }

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
            GameState gameState = boardController.getGameState();
            System.out.println("💀 GAME OVER (" + reason + ")");
            System.out.println("   Final Score: " + gameState.getScore());
            System.out.println("   Lines Cleared: " + gameState.getLinesCleared());
        });
    }

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

    @Override
    public void onDebugInfoUpdated(String debugInfo) {
        if (System.getProperty("debug.mode") != null) {
            System.out.println("🐛 " + debugInfo);
        }
    }

    // ========== UI 업데이트 헬퍼 메서드들 ==========

    /**
     * 하나의 셀을 업데이트합니다
     */
    private void updateCellRectangle(int row, int col, Cell cell) {
        Rectangle rect = cellRectangles[row][col];

        if (cell.isOccupied()) {
            rect.setFill(getJavaFXColor(cell.getColor()));
            String colorClass = getCssColorClass(cell.getColor());
            clearCellColor(rect);
            if (colorClass != null) {
                rect.getStyleClass().add(colorClass);
            }
        } else {
            rect.setFill(Color.rgb(26, 26, 26));
            clearCellColor(rect);
        }
    }

    /**
     * 현재 테트로미노를 화면에 그립니다
     * 
     * 이 메서드는 onTetrominoMoved 이벤트가 발생할 때마다 호출됩니다.
     * 전체 보드를 다시 그려서 이전 위치의 테트로미노를 지웁니다.
     */
    private void drawCurrentTetromino() {
        GameState gameState = boardController.getGameState();
        
        // 전체 보드를 다시 그립니다
        Cell[][] grid = gameState.getGrid();
        for (int row = 0; row < gameState.getBoardHeight(); row++) {
            for (int col = 0; col < gameState.getBoardWidth(); col++) {
                updateCellRectangle(row, col, grid[row][col]);
            }
        }

        if (gameState.getCurrentTetromino() == null) {
            return;
        }

        // 현재 테트로미노를 그립니다
        int[][] shape = gameState.getCurrentTetromino().getCurrentShape();
        int pivotX = gameState.getCurrentTetromino().getPivotX();
        int pivotY = gameState.getCurrentTetromino().getPivotY();

        seoultech.se.core.model.enumType.Color tetrominoColor =
            gameState.getCurrentTetromino().getColor();

        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[0].length; col++) {
                if (shape[row][col] == 1) {
                    int absoluteX = gameState.getCurrentX() + (col - pivotX);
                    int absoluteY = gameState.getCurrentY() + (row - pivotY);

                    if (absoluteY >= 0 && absoluteY < gameState.getBoardHeight() &&
                        absoluteX >= 0 && absoluteX < gameState.getBoardWidth()) {

                        Rectangle rect = cellRectangles[absoluteY][absoluteX];
                        rect.setFill(getJavaFXColor(tetrominoColor));
                        
                        String colorClass = getCssColorClass(tetrominoColor);
                        clearCellColor(rect);
                        if (colorClass != null) {
                            rect.getStyleClass().add(colorClass);
                        }
                    }
                }
            }
        }
    }

    /**
     * 점수, 레벨, 라인 수 레이블을 업데이트합니다
     */
    private void updateGameInfoLabels() {
        GameState state = boardController.getGameState();
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

    private String getCssColorClass(seoultech.se.core.model.enumType.Color color) {
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

    private Color getJavaFXColor(seoultech.se.core.model.enumType.Color color) {
        switch (color) {
            case RED:     return Color.rgb(255, 68, 68);
            case GREEN:   return Color.rgb(68, 255, 68);
            case BLUE:    return Color.rgb(68, 68, 255);
            case YELLOW:  return Color.rgb(255, 255, 68);
            case CYAN:    return Color.rgb(68, 255, 255);
            case MAGENTA: return Color.rgb(255, 68, 255);
            case ORANGE:  return Color.rgb(255, 136, 68);
            default:      return Color.rgb(128, 128, 128);
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
