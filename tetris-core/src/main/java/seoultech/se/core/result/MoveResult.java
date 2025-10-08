package seoultech.se.core.result;

import lombok.Value;
import seoultech.se.core.GameState;

@Value
public class MoveResult{
    boolean success;
    GameState newState;
    String failureReason;


    public static MoveResult success(GameState newState) {
        return new MoveResult(true, newState, null);
    }

    public static MoveResult failed(GameState originalState, String reason) {
        return new MoveResult(false, originalState, reason);
    }
}
