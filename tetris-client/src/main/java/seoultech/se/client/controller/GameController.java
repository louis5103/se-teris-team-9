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
import seoultech.se.core.command.*;
import seoultech.se.core.model.BoardObserver;
import seoultech.se.core.model.Cell;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * JavaFX UI를 제어하는 컨트롤러
 * 
 * 이 클래스의 역할이 더 명확해졌습니다. 이제 다음 세 가지 일만 합니다:
 * 
 * 1. 사용자 입력을 Command로 변환
 *    키보드 이벤트를 받아서 적절한 Command 객체를 생성합니다.
 *    예: LEFT 키 → MoveCommand(Direction.LEFT)
 * 
 * 2. Command를 BoardController에 전달
 *    생성한 Command를 executeCommand()로 보냅니다.
 *    (나중에는 GameService로 보내게 될 것입니다)
 * 
 * 3. Event를 받아서 UI 업데이트
 *    BoardObserver로서 Event를 받으면, JavaFX UI를 업데이트합니다.
 *    이것은 기존과 동일합니다.
 * 
 * 주목할 점은 이제 GameController가 게임 로직을 전혀 모른다는 것입니다.
 * "왼쪽으로 이동할 수 있는가?", "라인이 완성되었는가?" 같은 판단을 하지 않습니다.
 * 단지 사용자가 무엇을 하고 싶어하는지를 Command로 표현하고, 결과를 Event로 받을 뿐입니다.
 * 
 * 이것이 바로 관심사의 분리입니다. UI는 UI 일만, 게임 로직은 GameEngine이,
 * 중재는 BoardController가 담당하는 거죠.
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
     * FXML이 로드된 후 자동으로 호출됩니다
     * 
     * 초기화 순서는 동일하지만, 이제 Command 기반으로 동작합니다.
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
     * 
     * 이 부분은 변경사항이 없습니다. UI 초기화는 게임 로직과 무관하니까요.
     */
    private void initializeGridPane(GameState gameState) {
        int width = gameState.getBoardWidth();
        int height = gameState.getBoardHeight();

        System.out.println("🎨 Initializing GridPane with " + width + "x" + height + " cells...");

        cellRectangles = new Rectangle[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Rectangle rect = new Rectangle(CELL_SIZE, CELL_SIZE);

                // 기본 색상 설정
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
     * 이제 게임 루프에서도 Command를 사용합니다!
     * 기존에는 `boardController.moveDown()`을 직접 호출했지만,
     * 이제는 `MoveCommand(Direction.DOWN)`을 생성해서 보냅니다.
     * 
     * 왜 이렇게 바꿨나요?
     * 
     * 일관성 때문입니다. 사용자가 DOWN 키를 눌러서 내려가는 것이나,
     * 게임 루프에서 자동으로 내려가는 것이나, 본질적으로 같은 행동입니다.
     * 둘 다 "블록을 아래로 이동"하고 싶은 거니까요.
     * 
     * 같은 행동이면 같은 Command를 사용하는 것이 맞습니다.
     * 이렇게 하면 코드가 단순해지고, 버그가 줄어듭니다.
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
                    // Command 패턴 사용!
                    // 기존: boardController.moveDown();
                    // 새로운: boardController.executeCommand(new MoveCommand(Direction.DOWN));
                    boardController.executeCommand(new MoveCommand(Direction.DOWN));
                    lastUpdateTime = now;
                }
            }
        };
    }

    /**
     * 키보드 입력을 처리합니다
     * 
     * 이것이 가장 크게 바뀐 부분입니다!
     * 이제 키 입력을 받으면 적절한 Command 객체를 생성합니다.
     * 
     * 각 키마다 어떤 Command를 만들어야 하는지 명확합니다:
     * - LEFT/RIGHT/DOWN: MoveCommand with Direction
     * - UP/Z: RotateCommand with RotationDirection
     * - SPACE: HardDropCommand
     * - C: HoldCommand
     * 
     * 이 매핑은 매우 직관적입니다. 키보드의 물리적 입력을
     * 게임의 논리적 의도로 변환하는 거죠.
     */
    private void setupKeyboardControls() {
        boardGridPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(this::handleKeyPress);
                System.out.println("⌨️  Keyboard controls enabled");
            }
        });
    }

    /**
     * 키 입력을 Command로 변환하고 실행합니다
     * 
     * 이 메서드의 구조가 매우 깔끔해졌습니다.
     * 각 키에 대해 Command를 생성하고, 그것을 executeCommand()로 보내기만 하면 됩니다.
     * 
     * 기존 방식과 비교해보세요:
     * 
     * 기존:
     * ```java
     * case LEFT:
     *     boardController.moveLeft();
     *     break;
     * ```
     * 
     * 새로운:
     * ```java
     * case LEFT:
     *     command = new MoveCommand(Direction.LEFT);
     *     break;
     * ```
     * 
     * 차이가 작아 보이지만, 이것이 가져오는 변화는 엄청납니다.
     * Command 객체는 직렬화할 수 있고, 저장할 수 있고, 네트워크로 보낼 수 있습니다.
     * 반면 메서드 호출은 그 자리에서 즉시 실행될 뿐입니다.
     * 
     * Command 패턴을 사용하면 이 입력을 기록해서 리플레이를 만들 수도 있고,
     * 네트워크로 보내서 다른 플레이어와 대전할 수도 있습니다.
     */
    private void handleKeyPress(KeyEvent event) {
        if (boardController.getGameState().isGameOver()) {
            return;
        }

        GameCommand command = null;

        switch (event.getCode()) {
            case LEFT:
                command = new MoveCommand(Direction.LEFT);
                break;
                
            case RIGHT:
                command = new MoveCommand(Direction.RIGHT);
                break;
                
            case DOWN:
                command = new MoveCommand(Direction.DOWN);
                break;
                
            case UP:
                command = new RotateCommand(RotationDirection.CLOCKWISE);
                break;
                
            case Z:
                command = new RotateCommand(RotationDirection.COUNTER_CLOCKWISE);
                break;
                
            case SPACE:
                command = new HardDropCommand();
                break;
                
            case C:
                command = new HoldCommand();
                break;
        }

        // Command가 생성되었으면 실행
        if (command != null) {
            boardController.executeCommand(command);
        }

        event.consume();
    }

    // ========== BoardObserver 구현 ==========
    // 
    // 이 부분은 변경사항이 거의 없습니다.
    // BoardController가 Event를 발행하면, 이 메서드들이 호출되어 UI를 업데이트합니다.
    // 
    // 기존 방식과의 유일한 차이는, 이제 Event 객체에서 정보를 추출한다는 점입니다.
    // 하지만 BoardObserver 인터페이스가 Event를 직접 받지 않고 개별 파라미터로 받기 때문에,
    // BoardController에서 Event를 풀어서 전달합니다.
    //
    // 나중에 BoardObserver를 리팩토링하면 더 깔끔해질 것입니다.

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
    public void onTetrominoRotated(RotationDirection direction, int kickIndex, Tetromino tetromino) {
        System.out.println("🔄 Rotated " + direction + " (kick index: " + kickIndex + ")");
    }

    @Override
    public void onTetrominoRotationFailed(RotationDirection direction, Tetromino tetromino) {
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
    //
    // 이 부분들도 변경사항이 없습니다. UI 렌더링은 게임 로직과 독립적이니까요.

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
