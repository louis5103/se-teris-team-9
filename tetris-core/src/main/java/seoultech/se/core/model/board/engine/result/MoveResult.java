package seoultech.se.core.model.board.engine.result;

@Value
public class MoveResult{
    boolean success;
    GameState newState;
    String failureReason;


    public static MoveResult success(GameState newState) {
        return new MoveDownCommandResult(true, newState, null);
    }

    public static MoveResult failed(String reason) {
        return new MoveDownCommandResult(false, null, reason);
    }
}
