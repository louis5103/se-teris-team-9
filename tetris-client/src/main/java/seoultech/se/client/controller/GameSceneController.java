package seoultech.se.client.controller;

import org.springframework.stereotype.Component;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.board.Board;
import seoultech.se.core.model.board.Cell;

@Component
public class GameSceneController extends BaseController {
    @FXML
    private BorderPane rootPane; // The root pane for the game scene

    @FXML
    private GridPane boardGridPane; // The UI container for the Tetris board

    @FXML
    private GridPane nextPieceGridPane; // The UI container for the next piece preview

    @FXML
    private Label scoreLabel; // UI element to display the score

    // @FXML
    // private Text levelText; // UI element to display the level

    @FXML
    private Button startPauseResumeButton;

    @FXML
    private Button resetButton; // Button to reset the game

    @FXML
    private Button exitButton; // Button to exit the game, only accessible when game is paused

    // tetris-core logic
    // Observable properties for UI binding and listening
    private Board board;
    private ObjectProperty<Color>[][] boardDisplay;
    private ObjectProperty<Tetromino> nextTetromino;
    private Timeline mainLoop; // JavaFX Timeline for the game loop
    
    private enum GameState { READY, RUNNING, PAUSED, GAME_OVER };
    private GameState gameState = GameState.READY;

    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;
    private static final int TILE_SIZE = 30;
    private static final int NEXT_PIECE_GRID_SIZE = 4;
    private final IntegerProperty scoreProperty = new SimpleIntegerProperty(0);
    // private final IntegerProperty levelProperty = new SimpleIntegerProperty(1);

    @FXML
    public void initialize() {
        super.initialize();
        this.board = new Board();
        this.boardDisplay = new SimpleObjectProperty[BOARD_HEIGHT][BOARD_WIDTH];
        // for (int y = 0; y < BOARD_HEIGHT; y++) {
        //     ObservableList<Color> row = FXCollections.observableArrayList();
        //     for (int x = 0; x < BOARD_WIDTH; x++) {
        //         row.add(Color.WHITE);
        //     }
        //     boardDisplay.add(row);
        // }
        this.nextTetromino = new SimpleObjectProperty<>(null);
        scoreLabel.textProperty().bind(scoreProperty.asString("Score: %d"));
        // levelText.textProperty().bind(levelProperty.asString("Level: %d"));

        // exitButton.setVisible(false);

        initBoardUI();
        initNextPieceGridUI();
        initScoreLevelUI();
        // Ensure the scene is available before initializing key handlers
        Platform.runLater(() -> {
            javafx.scene.Scene scene = boardGridPane.getScene();
            if (scene != null) {
                initKeyHandlers(scene);
            }
        });
        updateUI();
    }

    @FXML
    private void handleStartPauseResume() {
        if (gameState == GameState.RUNNING) {
            // Pause the game
            mainLoop.pause();
            gameState = GameState.PAUSED;
            startPauseResumeButton.setText("Resume");
            exitButton.setVisible(true);
        } else if (gameState == GameState.PAUSED) {
            // Resume the game
            mainLoop.play();
            gameState = GameState.RUNNING;
            startPauseResumeButton.setText("Pause");
            exitButton.setVisible(false);
        } else if (gameState == GameState.READY) {
            // Start the game
            initGameLoop();
            mainLoop.play();
            gameState = GameState.RUNNING;
            startPauseResumeButton.setText("Start");
            resetButton.setVisible(true);
        }
    }

    @FXML
    private void handleReset() {
        if (mainLoop != null) {
            mainLoop.stop();
        }
        // board.reset();
        scoreProperty.set(0);
        // levelProperty.set(1);
        gameState = GameState.READY;
        startPauseResumeButton.setText("Start");
        startPauseResumeButton.setDisable(false);
        resetButton.setVisible(false);
        exitButton.setVisible(false);
        updateUI();
    }

    @FXML
    private void handleExit() {
        Platform.exit();
    }

    public void initKeyHandlers(javafx.scene.Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (gameState != GameState.RUNNING) {
                return;
            }
            KeyCode code = event.getCode();
            if (code == KeyCode.A || code == KeyCode.LEFT) {
                board.moveLeft();
                updateUI();
            } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
                board.moveRight();
                updateUI();
            } else if (code == KeyCode.S || code == KeyCode.DOWN) {
                board.moveDown();
                updateUI();
            } else if (code == KeyCode.W || code == KeyCode.UP) {
                board.rotateClockwise();
                updateUI();
            }
            updateUI();
            event.consume();
        });
    }

    private void initBoardUI() {
        boardGridPane.getChildren().clear();
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boardDisplay[y][x] = new SimpleObjectProperty<>(Color.WHITE);
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                tile.fillProperty().bind(boardDisplay[y][x]);
                tile.setStroke(Color.BLACK);
                boardGridPane.add(tile, x, y);
            }
        }
    }

    private void initNextPieceGridUI() {
        // nextPieceGridPane.getChildren().clear();
        // for (int y = 0; y < NEXT_PIECE_GRID_SIZE; y++) {
        //     for (int x = 0; x < NEXT_PIECE_GRID_SIZE; x++) {
        //         Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
        //         tile.setFill(Color.BLACK);
        //         tile.setStroke(Color.DARKGRAY);
        //         nextPieceGridPane.add(tile, x, y);
        //     }
        // }
    }

    private void initScoreLevelUI() {
        scoreProperty.set(0);
        // levelText.setText("Level: 1");
    }

    /**
     * Fetches the current state from the core Board and updates the observable properties.
     * This will trigger the listeners to update the UI.
     */
    private void updateUI() {
        //get board with current piece
        
        //draw accumulated blocks
        Cell[][] grid = board.getGrid();
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                boardDisplay[y][x].setValue(mapCoreColorToFXColor(grid[y][x].getColor()));
            }
        }

        //draw current piece
        Tetromino currentTetromino = board.getCurrentTetromino();
        if (currentTetromino != null) {
            int tetrominoX = board.getCurrentX();
            int tetrominoY = board.getCurrentY();
            int[][] shape = currentTetromino.getCurrentShape();

            for (int y = 0; y < shape.length; y++) {
                for (int x = 0; x < shape[y].length; x++) {
                    if (shape[y][x] != 0) {
                        int boardX = tetrominoX + (x - currentTetromino.getPivotX());
                        int boardY = tetrominoY + (y - currentTetromino.getPivotY());
                        if (boardX >= 0 && boardX < BOARD_WIDTH && boardY >= 0 && boardY < BOARD_HEIGHT) {
                            boardDisplay[boardY][boardX].setValue(mapCoreColorToFXColor(currentTetromino.getColor()));
                        }
                    }
                }
            }
        }

        //TODO : update next piece, score, level
        // Tetromino next = board.getNextPiece();
        // this.nextTetromino.set(next);
        // drawNextPiece(next);
        // scoreProperty.set(board.getScore());
        // levelProperty.set(board.getLevel());
    }
    
    private void initGameLoop() {
        mainLoop = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
                gameTick();
        }));
        mainLoop.setCycleCount(Timeline.INDEFINITE);
    }

    private void gameTick() {
        board.moveDown();
        // TODO : change from gamestate to fetching from tetris-core
        // TODO : check if game over
        // The board class should provide a method to check if the game is over
        if (gameState != GameState.GAME_OVER) {
            gameOver();
        } else {
            updateUI();
        }
    }

    private void gameOver() {
        mainLoop.stop();
        gameState = GameState.GAME_OVER;
        startPauseResumeButton.setText("Game Over");
        startPauseResumeButton.setDisable(true);
        exitButton.setVisible(true);
    }

    private void drawNextPiece(Tetromino piece) {
        //TODO : draw next piece in nextPieceGridPane, clear grid first

        // for (javafx.scene.Node node : nextPieceGridPane.getChildren()) {
        //     ((Rectangle) node).setFill(Color.BLACK);
        // }

        // if (piece == null) return;

        // seoultech.se.core.model.block.enumType.Color pieceColor = piece.getColor();
        // int[][] shape = piece.getShape();

        // int offsetX = (NEXT_PIECE_GRID_SIZE - shape[0].length) / 2;
        // int offsetY = (NEXT_PIECE_GRID_SIZE - shape.length) / 2;

        // for (int y = 0; y < shape.length; y++) {
        //     for (int x = 0; x < shape[y].length; x++) {
        //         if (shape[y][x] != 0) {
        //             int gridX = x + offsetX;
        //             int gridY = y + offsetY;
        //             if (gridX >= 0 && gridX < NEXT_PIECE_GRID_SIZE && gridY >= 0 && gridY < NEXT_PIECE_GRID_SIZE) {
        //                 int index = gridY * NEXT_PIECE_GRID_SIZE + gridX;
        //                 if (index < nextPieceGridPane.getChildren().size()) {
        //                     Rectangle tile = (Rectangle) nextPieceGridPane.getChildren().get(index);
        //                     tile.setFill(mapCoreColorToFXColor(pieceColor));
        //                 }
        //             }
        //         }
        //     }
        // }
    }


    private Color mapCoreColorToFXColor(seoultech.se.core.model.block.enumType.Color coreColor) {
        if (coreColor == null) {
            return Color.WHITE;
        }
        switch (coreColor) {
            case RED: return Color.RED;
            case GREEN: return Color.GREEN;
            case BLUE: return Color.BLUE;
            case YELLOW: return Color.YELLOW;
            case CYAN: return Color.CYAN;
            case MAGENTA: return Color.MAGENTA;
            case ORANGE: return Color.ORANGE;
            default: return Color.WHITE;
        }
    }
}