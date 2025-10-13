package seoultech.se.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import seoultech.se.client.constants.UIConstants;
import seoultech.se.client.service.KeyMappingService;
import seoultech.se.client.service.NavigationService;
import seoultech.se.client.ui.BoardRenderer;
import seoultech.se.client.ui.GameInfoManager;
import seoultech.se.client.ui.GameLoopManager;
import seoultech.se.client.ui.InputHandler;
import seoultech.se.client.ui.NotificationManager;
import seoultech.se.client.ui.PopupManager;
import seoultech.se.client.util.ColorMapper;
import seoultech.se.core.BoardObserver;
import seoultech.se.core.GameState;
import seoultech.se.core.command.Direction;
import seoultech.se.core.command.MoveCommand;
import seoultech.se.core.model.Cell;
import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * JavaFX UI를 제어하는 컨트롤러
 * 
 * 리팩토링을 통해 책임이 더욱 명확해졌습니다:
 * 
 * 1. 사용자 입력을 Command로 변환
 * 2. Command를 BoardController에 전달
 * 3. BoardObserver로서 Event를 받아 UI 업데이트 위임
 * 
 * UI 관련 세부 작업은 다음 클래스들에 위임됩니다:
 * - NotificationManager: 알림 메시지 관리
 * - BoardRenderer: 보드 렌더링
 * - GameLoopManager: 게임 루프 관리
 * - PopupManager: 팝업 오버레이 관리
 * - InputHandler: 키보드 입력 처리 및 Command 변환
 * - GameInfoManager: 게임 정보 레이블 업데이트
 */
@Component
public class GameController implements BoardObserver {

    // FXML UI 요소들
    @FXML private GridPane boardGridPane;
    @FXML private GridPane holdGridPane;
    @FXML private GridPane nextGridPane;
    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label linesLabel;
    @FXML private Label gameOverLabel;
    @FXML private HBox topEventLine;
    @FXML private Label comboLabel;
    @FXML private Label lineClearTypeLabel;
    @FXML private Label backToBackLabel;
    @FXML private Label lineClearNotificationLabel;
    
    // 팝업 오버레이 요소들
    @FXML private javafx.scene.layout.VBox pauseOverlay;
    @FXML private javafx.scene.layout.VBox gameOverOverlay;
    @FXML private Label finalScoreLabel;

    @Autowired
    private KeyMappingService keyMappingService;

    @Autowired
    private NavigationService navigationService;

    // 게임 로직 컨트롤러
    private BoardController boardController;
    
    // UI 관리 클래스들
    private BoardRenderer boardRenderer;
    private NotificationManager notificationManager;
    private GameLoopManager gameLoopManager;
    private PopupManager popupManager;
    private InputHandler inputHandler;
    private GameInfoManager gameInfoManager;
    
    // Rectangle 배열들
    private Rectangle[][] cellRectangles;
    private Rectangle[][] holdCellRectangles;
    private Rectangle[][] nextCellRectangles;

    /**
     * FXML이 로드된 후 자동으로 호출됩니다
     */
    @FXML
    public void initialize() {
        System.out.println("🎮 GameController initializing...");

        // KeyMappingService 확인
        if (keyMappingService != null) {
            System.out.println("✅ KeyMappingService is ready");
            keyMappingService.printCurrentMappings();
        } else {
            System.err.println("❌ KeyMappingService is null!");
        }

        // BoardController 생성 및 Observer 등록
        boardController = new BoardController();
        boardController.addObserver(this);

        GameState gameState = boardController.getGameState();
        System.out.println("📊 Board created: " + gameState.getBoardWidth() + "x" + gameState.getBoardHeight());

        // UI 초기화
        initializeGridPane(gameState);
        initializePreviewPanes();
        
        // UI 관리 클래스들 초기화
        initializeManagers();
        
        gameInfoManager.updateAll(gameState);
        setupKeyboardControls();
        startGame();

        System.out.println("✅ GameController initialization complete!");
    }
    
    /**
     * UI 관리 클래스들을 초기화합니다
     */
    private void initializeManagers() {
        // NotificationManager 초기화
        notificationManager = new NotificationManager(
            topEventLine,
            comboLabel,
            lineClearTypeLabel,
            backToBackLabel,
            lineClearNotificationLabel
        );
        
        // BoardRenderer 초기화
        boardRenderer = new BoardRenderer(
            cellRectangles,
            holdCellRectangles,
            nextCellRectangles
        );
        
        // GameLoopManager 초기화
        gameLoopManager = new GameLoopManager();
        gameLoopManager.setCallback(() -> {
            GameState gameState = boardController.getGameState();
            
            if (gameState.isGameOver()) {
                return false; // 게임 루프 중지
            }
            
            if (gameState.isPaused()) {
                return true; // 일시정지 중이면 블록 낙하 안 함, 루프는 계속
            }
            
            // 블록 자동 낙하
            boardController.executeCommand(new MoveCommand(Direction.DOWN));
            return true; // 게임 루프 계속
        });
        
        // PopupManager 초기화
        popupManager = new PopupManager(
            pauseOverlay,
            gameOverOverlay,
            finalScoreLabel
        );
        
        // PopupManager 콜백 설정
        popupManager.setCallback(new PopupManager.PopupActionCallback() {
            @Override
            public void onResumeRequested() {
                resumeGame();
            }
            
            @Override
            public void onQuitRequested() {
                try {
                    navigationService.navigateTo("/view/main-view.fxml");
                } catch (Exception e) {
                    System.err.println("❌ Failed to navigate to main view: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onMainMenuRequested() {
                try {
                    navigationService.navigateTo("/view/main-view.fxml");
                } catch (Exception e) {
                    System.err.println("❌ Failed to navigate to main view: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onRestartRequested() {
                try {
                    navigationService.navigateTo("/view/game-view.fxml");
                } catch (Exception e) {
                    System.err.println("❌ Failed to restart game: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
        
        // InputHandler 초기화
        inputHandler = new InputHandler(keyMappingService);
        inputHandler.setCallback(command -> {
            boardController.executeCommand(command);
        });
        inputHandler.setGameStateProvider(new InputHandler.GameStateProvider() {
            @Override
            public boolean isGameOver() {
                return boardController.getGameState().isGameOver();
            }
            
            @Override
            public boolean isPaused() {
                return boardController.getGameState().isPaused();
            }
        });
        
        // GameInfoManager 초기화
        gameInfoManager = new GameInfoManager(
            scoreLabel,
            levelLabel,
            linesLabel
        );
    }

    /**
     * GridPane을 초기화하고 모든 셀의 Rectangle을 생성합니다
     */
    private void initializeGridPane(GameState gameState) {
        int width = gameState.getBoardWidth();
        int height = gameState.getBoardHeight();

        System.out.println("🎨 Initializing GridPane with " + width + "x" + height + " cells...");

        // GridPane 기본 설정
        boardGridPane.setHgap(0);
        boardGridPane.setVgap(0);
        
        // GridPane 크기 고정
        double boardWidth = width * UIConstants.CELL_SIZE;
        double boardHeight = height * UIConstants.CELL_SIZE;
      
        boardGridPane.setPrefSize(boardWidth, boardHeight);
        boardGridPane.setMinSize(boardWidth, boardHeight);
        boardGridPane.setMaxSize(boardWidth, boardHeight);
        
        cellRectangles = new Rectangle[height][width];

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Rectangle rect = new Rectangle(UIConstants.CELL_SIZE, UIConstants.CELL_SIZE);

                // 기본 색상 설정
                rect.setFill(ColorMapper.getEmptyCellColor());
                rect.setStroke(ColorMapper.getCellBorderColor());
                rect.setStrokeWidth(UIConstants.CELL_BORDER_WIDTH);
                
                // 픽셀 정렬로 떨림 방지
                rect.setSmooth(false);
                rect.setCache(true);

                // CSS 클래스 추가
                rect.getStyleClass().add(UIConstants.BOARD_CELL_CLASS);

                // GridPane에 추가
                boardGridPane.add(rect, col, row);
                cellRectangles[row][col] = rect;
            }
        }

        System.out.println("✅ GridPane initialized with " + (width * height) + " cells");
    }
    
    /**
     * Hold와 Next 미리보기 영역 초기화
     */
    private void initializePreviewPanes() {
        // Hold 영역 초기화
        holdCellRectangles = new Rectangle[UIConstants.PREVIEW_GRID_ROWS][UIConstants.PREVIEW_GRID_COLS];
        initializePreviewGrid(holdGridPane, holdCellRectangles, 
                            UIConstants.PREVIEW_GRID_ROWS, UIConstants.PREVIEW_GRID_COLS);
        
        // Next 영역 초기화
        nextCellRectangles = new Rectangle[UIConstants.PREVIEW_GRID_ROWS][UIConstants.PREVIEW_GRID_COLS];
        initializePreviewGrid(nextGridPane, nextCellRectangles, 
                            UIConstants.PREVIEW_GRID_ROWS, UIConstants.PREVIEW_GRID_COLS);
    }
    
    /**
     * 미리보기 그리드 초기화 헬퍼 메서드
     */
    private void initializePreviewGrid(GridPane gridPane, Rectangle[][] rectangles, int rows, int cols) {
        // GridPane 기본 설정
        gridPane.setHgap(0);
        gridPane.setVgap(0);
        
        // GridPane 크기 고정
        double gridWidth = cols * UIConstants.PREVIEW_CELL_SIZE;
        double gridHeight = rows * UIConstants.PREVIEW_CELL_SIZE;
        gridPane.setPrefSize(gridWidth, gridHeight);
        gridPane.setMinSize(gridWidth, gridHeight);
        gridPane.setMaxSize(gridWidth, gridHeight);
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Rectangle rect = new Rectangle(UIConstants.PREVIEW_CELL_SIZE, UIConstants.PREVIEW_CELL_SIZE);
                rect.setFill(ColorMapper.getEmptyCellColor());
                rect.setStroke(ColorMapper.getCellBorderColor());
                rect.setStrokeWidth(UIConstants.CELL_BORDER_WIDTH);
                
                // 픽셀 정렬로 떨림 방지
                rect.setSmooth(false);
                rect.setCache(true);
                
                // CSS 클래스 추가
                rect.getStyleClass().add(UIConstants.PREVIEW_CELL_CLASS);
                
                gridPane.add(rect, col, row);
                rectangles[row][col] = rect;
            }
        }
    }

    /**
     * 키보드 입력을 처리합니다
     */
    private void setupKeyboardControls() {
        inputHandler.setupKeyboardControls(boardGridPane);
    }

    // ========== BoardObserver 구현 ==========
    
    @Override
    public void onCellChanged(int row, int col, Cell cell) {
        boardRenderer.updateCell(row, col, cell);
    }

    @Override
    public void onMultipleCellsChanged(int[] rows, int[] cols, Cell[][] cells) {
        // 대량 셀 업데이트 (필요시 성능 최적화 구현)
    }

    @Override
    public void onTetrominoMoved(int x, int y, Tetromino tetromino) {
        boardRenderer.drawBoard(boardController.getGameState());
    }

    @Override
    public void onTetrominoRotated(RotationDirection direction, int kickIndex, Tetromino tetromino) {
        boardRenderer.drawBoard(boardController.getGameState());
    }

    @Override
    public void onTetrominoRotationFailed(RotationDirection direction) {
        // 실패 사운드나 시각 효과 추가 가능
    }

    @Override
    public void onTetrominoLocked(Tetromino tetromino) {
        // 블록 고정 애니메이션 효과 추가 가능
    }

    @Override
    public void onTetrominoLockDelayStarted() {
        // Lock Delay 시각적 표시 (예: 블록 깜빡임)
    }

    @Override
    public void onTetrominoLockDelayReset(int remainingResets) {
        // Lock Delay 리셋 횟수 표시
    }

    @Override
    public void onTetrominoSpawned(Tetromino tetromino) {
        boardRenderer.drawBoard(boardController.getGameState());
    }

    @Override
    public void onNextQueueUpdated(TetrominoType[] nextPieces) {
        if (nextPieces != null && nextPieces.length > 0) {
            boardRenderer.drawNextPiece(nextPieces[0]);
        }
    }

    @Override
    public void onHoldChanged(TetrominoType heldPiece, TetrominoType previousPiece) {
        boardRenderer.drawHoldPiece(heldPiece);
    }

    @Override
    public void onHoldFailed() {
        notificationManager.showLineClearType("⚠️ Hold already used!");
    }

    @Override
    public void onLineCleared(int linesCleared, int[] clearedRows,
                              boolean isTSpin, boolean isTSpinMini, boolean isPerfectClear) {
        StringBuilder message = new StringBuilder();
        
        // T-Spin 표시
        if (isTSpin) {
            message.append(isTSpinMini ? "T-SPIN MINI " : "T-SPIN ");
        }
        
        // 라인 타입 표시
        switch (linesCleared) {
            case 1:
                message.append("SINGLE");
                break;
            case 2:
                message.append("DOUBLE");
                break;
            case 3:
                message.append("TRIPLE");
                break;
            case 4:
                message.append("TETRIS");
                break;
        }
        
        // Perfect Clear 표시
        if (isPerfectClear) {
            message.append(" 🌟 PERFECT CLEAR!");
        }
        
        // 중앙에 라인 클리어 타입 표시
        if (message.length() > 0) {
            notificationManager.showLineClearType(message.toString());
        }
        
        // 우측에 라인 클리어 수 표시
        GameState state = boardController.getGameState();
        notificationManager.showLineClearCount(linesCleared, state.getLinesCleared());
    }

    @Override
    public void onCombo(int comboCount) {
        notificationManager.showCombo("🔥 COMBO x" + comboCount);
    }

    @Override
    public void onComboBreak(int finalComboCount) {
        // Combo 종료는 메시지 표시 안 함
    }

    @Override
    public void onBackToBack(int backToBackCount) {
        notificationManager.showBackToBack("⚡ B2B x" + backToBackCount);
    }

    @Override
    public void onBackToBackBreak(int finalBackToBackCount) {
        // B2B 종료는 메시지 표시 안 함
    }

    @Override
    public void onScoreAdded(long points, String reason) {
        // 점수는 onGameStateChanged에서 자동 업데이트됨
    }

    @Override
    public void onGameStateChanged(GameState gameState) {
        Platform.runLater(() -> {
            gameInfoManager.updateAll(gameState);
            gameLoopManager.updateDropSpeed(gameState);
        });
    }

    @Override
    public void onLevelUp(int newLevel) {
        notificationManager.showLineClearType("📈 LEVEL UP! - Level " + newLevel);
    }

    @Override
    public void onGamePaused() {
        pauseGame();
        popupManager.showPausePopup();
    }

    @Override
    public void onGameResumed() {
        // UI만 업데이트 (Command는 이미 실행된 상태)
        gameLoopManager.resume();
        notificationManager.hideAllNotifications();
    }

    @Override
    public void onGameOver(String reason) {
        Platform.runLater(() -> {
            gameOverLabel.setVisible(true);
            GameState gameState = boardController.getGameState();
            System.out.println("💀 GAME OVER (" + reason + ")");
            System.out.println("   Final Score: " + gameState.getScore());
            System.out.println("   Lines Cleared: " + gameState.getLinesCleared());
            popupManager.showGameOverPopup(gameState.getScore());
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
    // GameInfoManager로 이동됨

    // ========== 게임 제어 ==========
    public void startGame() {
        gameOverLabel.setVisible(false);
        gameLoopManager.start();
        boardGridPane.requestFocus();
        System.out.println("🎮 Game Started!");
    }

    public void pauseGame() {
        gameLoopManager.pause();
        notificationManager.showLineClearType("⏸️ PAUSED - Press P to resume");
    }

    public void resumeGame() {
        gameLoopManager.resume();
        notificationManager.hideAllNotifications();
        // Resume Command 실행하여 게임 상태도 업데이트
        boardController.executeCommand(new seoultech.se.core.command.ResumeCommand());
    }

    // ========== 오버레이 버튼 핸들러 ==========
    // PopupManager로 위임

    @FXML
    private void handleResumeFromOverlay() {
        popupManager.handleResumeAction();
    }

    @FXML
    private void handleQuitFromOverlay() {
        popupManager.handleQuitAction();
    }

    @FXML
    private void handleMainFromOverlay() {
        popupManager.handleMainMenuAction();
    }

    @FXML
    private void handleRestartFromOverlay() {
        popupManager.handleRestartAction();
    }
}
