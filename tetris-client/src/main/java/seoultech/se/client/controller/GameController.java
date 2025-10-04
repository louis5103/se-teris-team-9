package seoultech.se.client.controller;

import org.springframework.stereotype.Component;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
import seoultech.se.core.BoardObserver;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.command.implement.moveCommand.HardDropCommand;
import seoultech.se.core.command.implement.moveCommand.HoldCommand;
import seoultech.se.core.command.implement.moveCommand.MoveDownCommand;
import seoultech.se.core.command.implement.moveCommand.MoveRightCommand;
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
 *
 * 구현 우선순위 가이드:
 * ⭐⭐⭐ 필수: 게임의 기본 동작에 필요
 * ⭐⭐ 중요: 게임 경험을 향상시킴
 * ⭐ 선택: 추가 기능 또는 디버그용
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
        board = new Board();
        board.addObserver(this);  // 이 Controller를 Observer로 등록

        initializeGridPane();
        updateGameInfoLabels();
        setupGameLoop();
        setupKeyboardControls();

        board.spawnNewTetromino();
        startGame();
    }

    private void initializeGridPane() {
        int width = board.getBoardWidth();
        int height = board.getBoardHeight();

        cellRectangles = new Rectangle[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);
                rect.getStyleClass().add("board-cell");
                boardGridPane.add(rect, col, row);
                cellRectangles[row][col] = rect;
            }
        }
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

    /**
     * ⭐⭐⭐ 필수: 셀이 변경될 때마다 호출됨
     */
    @Override
    public void onCellChanged(int row, int col, Cell cell) {
        Platform.runLater(() -> {
            updateCellRectangle(row, col, cell);
        });
    }

    /**
     * ⭐ 선택: 최적화를 위한 메서드 (여러 셀을 한번에 업데이트)
     */
    @Override
    public void onMultipleCellsChanged(int[] rows, int[] cols, Cell[][] cells) {
        // TODO: 구현하면 성능 향상 가능
        // 당장은 onCellChanged가 여러 번 호출되는 것으로 충분
    }

    // ========== BoardObserver 구현 - 테트로미노 이동/회전 ==========

    /**
     * ⭐⭐⭐ 필수: 테트로미노가 이동할 때마다 호출됨
     */
    @Override
    public void onTetrominoMoved(int x, int y, Tetromino tetromino) {
        Platform.runLater(() -> {
            drawCurrentTetromino();
        });
    }

    /**
     * ⭐⭐ 중요: 회전 애니메이션이나 사운드 추가 시 사용
     */
    @Override
    public void onTetrominoRotated(RotationDirection direction, int kickIndex) {
        System.out.println("🔄 Rotated " + direction + " (kick index: " + kickIndex + ")");
        // TODO: 회전 사운드 재생
        // TODO: kickIndex가 0이 아니면 Wall Kick 이펙트
    }

    /**
     * ⭐ 선택: 회전 실패 피드백 (진동, 사운드 등)
     */
    @Override
    public void onTetrominoRotationFailed(RotationDirection direction) {
        System.out.println("❌ Rotation failed: " + direction);
        // TODO: 실패 사운드 재생
    }

    /**
     * ⭐⭐ 중요: 블록 고정 시 사운드/이펙트
     */
    @Override
    public void onTetrominoLocked(Tetromino tetromino) {
        System.out.println("🔒 Tetromino locked: " + tetromino.getType());
        // TODO: 고정 사운드 재생
    }

    /**
     * ⭐ 선택: Lock Delay 구현 시 사용
     */
    @Override
    public void onTetrominoLockDelayStarted() {
        // TODO: Lock Delay 타이머 UI 표시
    }

    /**
     * ⭐ 선택: Lock Delay 구현 시 사용
     */
    @Override
    public void onTetrominoLockDelayReset(int remainingResets) {
        // TODO: 남은 리셋 횟수 표시
    }

    // ========== BoardObserver 구현 - 테트로미노 생성 ==========

    /**
     * ⭐⭐⭐ 필수: 새 블록이 생성될 때 호출됨
     */
    @Override
    public void onTetrominoSpawned(Tetromino tetromino) {
        Platform.runLater(() -> {
            drawCurrentTetromino();
        });
        System.out.println("🎲 New tetromino spawned: " + tetromino.getType());
    }

    /**
     * ⭐⭐ 중요: Next 블록 미리보기 구현 시 사용
     */
    @Override
    public void onNextQueueUpdated(TetrominoType[] nextPieces) {
        System.out.println("📋 Next queue updated: " + java.util.Arrays.toString(nextPieces));
        // TODO: Next 블록 UI 업데이트
        // Platform.runLater(() -> drawNextPieces(nextPieces));
    }

    // ========== BoardObserver 구현 - Hold 시스템 ==========

    /**
     * ⭐⭐ 중요: Hold UI 구현 시 사용
     */
    @Override
    public void onHoldChanged(TetrominoType heldPiece, TetrominoType previousPiece) {
        System.out.println("💾 Hold changed: " + heldPiece);
        // TODO: Hold UI 업데이트
        // Platform.runLater(() -> drawHoldPiece(heldPiece));
    }

    /**
     * ⭐⭐ 중요: Hold 실패 피드백
     */
    @Override
    public void onHoldFailed() {
        System.out.println("⚠️ Hold failed (already used this turn)");
        // TODO: 실패 피드백 (화면 흔들림, 사운드 등)
    }

    // ========== BoardObserver 구현 - 라인 클리어 ==========

    /**
     * ⭐⭐⭐ 필수: 라인 클리어 시 호출됨
     */
    @Override
    public void onLineCleared(int linesCleared, int[] clearedRows,
                              boolean isTSpin, boolean isTSpinMini, boolean isPerfectClear) {
        String clearType = isTSpin ? "T-SPIN " : "";
        if (isTSpinMini) clearType += "MINI ";

        System.out.println("✨ Line cleared: " + clearType + linesCleared + " lines");

        if (isPerfectClear) {
            System.out.println("🌟 PERFECT CLEAR!");
            // TODO: Perfect Clear 애니메이션
        }

        // TODO: 라인 클리어 애니메이션
        // TODO: 사운드 재생 (SINGLE, DOUBLE, TRIPLE, TETRIS, T-SPIN 별로 다르게)
    }

    /**
     * ⭐⭐ 중요: 콤보 표시
     */
    @Override
    public void onCombo(int comboCount) {
        System.out.println("🔥 COMBO x" + comboCount);
        // TODO: 콤보 UI 표시
        // Platform.runLater(() -> showComboText(comboCount));
    }

    /**
     * ⭐⭐ 중요: 콤보 종료
     */
    @Override
    public void onComboBreak(int finalComboCount) {
        System.out.println("💨 Combo ended: " + finalComboCount);
        // TODO: 콤보 UI 숨기기
    }

    /**
     * ⭐⭐ 중요: Back-to-Back 표시
     */
    @Override
    public void onBackToBack(int backToBackCount) {
        System.out.println("⚡ BACK-TO-BACK x" + backToBackCount);
        // TODO: B2B UI 표시
    }

    /**
     * ⭐⭐ 중요: Back-to-Back 종료
     */
    @Override
    public void onBackToBackBreak(int finalBackToBackCount) {
        System.out.println("💨 B2B ended: " + finalBackToBackCount);
        // TODO: B2B UI 숨기기
    }

    // ========== BoardObserver 구현 - 점수 및 게임 상태 ==========

    /**
     * ⭐⭐ 중요: 점수 획득 시 이유와 함께 표시
     */
    @Override
    public void onScoreAdded(long points, String reason) {
        System.out.println("💰 +" + points + " points (" + reason + ")");
        // TODO: 점수 획득 애니메이션 (화면에 "+100 SINGLE" 같은 텍스트 표시)
    }

    /**
     * ⭐⭐⭐ 필수: 게임 상태 변경 시 UI 업데이트
     */
    @Override
    public void onGameStateChanged(GameState gameState) {
        Platform.runLater(() -> {
            updateGameInfoLabels();

            // 레벨에 따라 낙하 속도 조정
            dropInterval = Math.max(100_000_000L,
                                   500_000_000L - (gameState.getLevel() * 50_000_000L));
        });
    }

    /**
     * ⭐⭐ 중요: 레벨업 이펙트
     */
    @Override
    public void onLevelUp(int newLevel) {
        System.out.println("📈 LEVEL UP! Now at level " + newLevel);
        // TODO: 레벨업 애니메이션/사운드
    }

    // ========== BoardObserver 구현 - 게임 진행 ==========

    /**
     * ⭐⭐ 중요: 일시정지 UI
     */
    @Override
    public void onGamePaused() {
        System.out.println("⏸️ Game paused");
        // TODO: 일시정지 오버레이 표시
    }

    /**
     * ⭐⭐ 중요: 일시정지 해제
     */
    @Override
    public void onGameResumed() {
        System.out.println("▶️ Game resumed");
        // TODO: 일시정지 오버레이 숨기기
    }

    /**
     * ⭐⭐⭐ 필수: 게임 오버 처리
     */
    @Override
    public void onGameOver(String reason) {
        Platform.runLater(() -> {
            gameOverLabel.setVisible(true);
            System.out.println("💀 GAME OVER (" + reason + ")");
            System.out.println("   Final Score: " + board.getGameState().getScore());
            System.out.println("   Lines Cleared: " + board.getGameState().getLinesCleared());
            // TODO: 게임 오버 화면 표시 (최종 점수, 통계 등)
        });
    }

    // ========== BoardObserver 구현 - 멀티플레이어 ==========

    /**
     * ⭐⭐ 중요: 멀티플레이어 구현 시 사용
     */
    @Override
    public void onGarbageLinesAdded(int lines, String sourcePlayerId) {
        System.out.println("💥 Received " + lines + " garbage lines from " + sourcePlayerId);
        // TODO: 쓰레기 라인 경고 UI
    }

    /**
     * ⭐⭐ 중요: 멀티플레이어 구현 시 사용
     */
    @Override
    public void onGarbageLinesCleared(int lines) {
        System.out.println("🛡️ Cleared " + lines + " incoming garbage lines");
        // TODO: 방어 성공 이펙트
    }

    /**
     * ⭐⭐ 중요: 멀티플레이어 구현 시 사용
     */
    @Override
    public void onAttackSent(String targetPlayerId, int lines) {
        System.out.println("⚔️ Sent " + lines + " lines to " + targetPlayerId);
        // TODO: 공격 이펙트
    }

    // ========== BoardObserver 구현 - 디버그 ==========

    /**
     * ⭐ 선택: 개발 중 디버그 정보 표시
     */
    @Override
    public void onDebugInfoUpdated(String debugInfo) {
        // 프로덕션에서는 무시
        if (System.getProperty("debug.mode") != null) {
            System.out.println("🐛 " + debugInfo);
        }
    }

    // ========== UI 업데이트 헬퍼 메서드들 ==========

    private void updateCellRectangle(int row, int col, Cell cell) {
        Rectangle rect = cellRectangles[row][col];

        if (cell.isOccupied()) {
            String colorClass = getCssColorClass(cell.getColor());
            clearCellColor(rect);
            if (colorClass != null) {
                rect.getStyleClass().add(colorClass);
            }
        } else {
            clearCellColor(rect);
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
