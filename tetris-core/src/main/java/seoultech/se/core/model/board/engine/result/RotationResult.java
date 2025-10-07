package seoultech.se.core.model.board.engine.result;

import lombok.Value;
import seoultech.se.core.model.block.enumType.RotationDirection;
import seoultech.se.core.model.board.GameState;

@Value
public class RotationResult {
    boolean success;
    GameState newState;
    RotationDirection direction;
    int kickIndex; // 회전 시도 시도한 KICK 인덱스. (성공 시에만 의미 있음), 0~4는 성공, -1이면 실패.
    String failureReason;


    public static RotationResult success(GameState newState, RotationDirection direction, int kickIndex) {
        return new RotationResult(true, newState, direction, kickIndex, null);
    }

    public static RotationResult failed(GameState originalState, String reason) {
        return new RotationResult(false, originalState, null, -1, reason);
    }
}
