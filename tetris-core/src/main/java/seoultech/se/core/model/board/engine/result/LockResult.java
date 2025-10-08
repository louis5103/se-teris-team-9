package seoultech.se.core.model.board.engine.result;

@Data
public class LockResult {
    boolean gameOver;
    GameState newState;
    LineClearResult lineClearResult;
    String gameOverReason;

    public static LockResult success(GameState newState, LineClearResult clearResult) {
        return new LockResult(false, newState, clearResult, null);
    }

    public static LockResult gameOver(GameState newState, String reason) {
        return new LockResult(true, newState, null, reason);
    }
}
