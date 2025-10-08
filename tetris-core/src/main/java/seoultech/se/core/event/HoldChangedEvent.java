package seoultech.se.core.event;

import lombok.Getter;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * Hold 블록이 변경되었을 때 발생하는 이벤트
 */
@Getter
public class HoldChangedEvent implements GameEvent {
    private final TetrominoType newHeldPiece;
    private final TetrominoType previousHeldPiece;
    private final long timestamp;
    
    public HoldChangedEvent(TetrominoType newHeldPiece, TetrominoType previousHeldPiece) {
        this.newHeldPiece = newHeldPiece;
        this.previousHeldPiece = previousHeldPiece;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.HOLD_CHANGED;
    }
}
