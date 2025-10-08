package seoultech.se.core.model.board;

/**
 * 게임 엔진 클래스
 * Input: GameState
 * Output: GameState
 */
public class GameEngine {
    public static MoveResult tryMoveLeft(GameState state) {
        GameState newState = state.deepCopy();
        int newX = state.getCurrentX() - 1;

        if(isValidPosition(state, state.getCurrentTetromino(), newX, state.getCurrentY())) {
            newState.setCurrentX(newX);
            return MoveResult.success(newState);
        } else {
            return MoveResult.failed(state, "[GameEngine] (Method: tryMoveLeft) Cannot move left : Blocked or out of bounds");
        }
    }

    public static RotationResult tryRotate(GameState state, RotationDirection direction) {
        GameState newState = state.deepCopy();
        Tetromino rotated = state.getCurrentTetromino().getRotatedInstance(direction);
        int[][] kickData = WallKickEventData.getKickDate(
                state.getCurrentTetromino().getType(),
                state.getCurrentTetromino().getRotationState(),
                rotated.getRotationState()
        );

        // wall kick 시도
        for(int kickIndex = 0; kickIndex < kickData.length; kickIndex++) {
            int[] offset = kickData[kickIndex];
            int newX = state.getCurrentX() + offset[0];
            int newY = state.getCurrentY() + offset[1];

            if(isValidPosition(state, rotated, newX, newY)) {
                newState.setCurrentTetromino(rotated);
                newState.setCurrentX(newX);
                newState.setCurrentY(newY);
                return RotationResult.success(newState, kickIndex);
            }
        }
        return RotationResult.failed(state, "[GameEngine] (Method: tryRotate) Cannot rotate : Blocked or out of bounds");
    }

    public static LockResult lockTetromino(GameState state) {
        GameState = newState = state.deepCopy();

        int[][] shape = state.getCurrentTetromino().getCurrentShape();
        for(int row = 0; row < shape.length; row++) {
            for(int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    int absX = state.getCurrentX() + (col - state.getCurrentTetromino().getPivotX());
                    int absY = state.getCurrentY() + (row - state.getCurrentTetromino().getPivotY());

                    if( absY < 0 ) {
                        // 게임 오버 처리
                        newState.setIsGameOver(1);
                        return LockResult.gameOver(newState, "[GameEngine] (Method: lockTetromino) Game Over: Block locked above the board");
                    }

                    newState.getGrid()[absY][absX] = Cell.filled(state.getCurrentTetromino().getType().getColor());
                    newState.getGrid()[absY][absX].setOccupiedBy(state.getCurrentTetromino().getType());
                }
            }
        }
    }
}
