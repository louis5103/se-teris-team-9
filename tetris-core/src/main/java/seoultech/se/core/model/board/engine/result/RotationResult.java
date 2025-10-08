package seoultech.se.core.model.board.engine.result;

@Value
public class RotationResult {
    boolean success;
    GameState newState;
    int kickIndex; // 회전 시도 시도한 KICK 인덱스. (성공 시에만 의미 있음)
    String failureReason;


    public static RotationResult success(GameState newState, int kickIndex) {
        return new RotationResult(true, newState, kickIndex, null);
    }

    public static RotationResult failed(GameState originalState, String reason) {
        return new RotationResult(false, originalState, -1, reason);
    }
}
