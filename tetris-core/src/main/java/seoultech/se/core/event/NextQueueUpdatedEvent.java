package seoultech.se.core.event;

import lombok.Getter;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * Next Queue가 업데이트되었을 때 발생하는 이벤트
 */
@Getter
public class NextQueueUpdatedEvent implements GameEvent {
    private final TetrominoType[] nextQueue;
    private final long timestamp;
    
    public NextQueueUpdatedEvent(TetrominoType[] nextQueue) {
        this.nextQueue = nextQueue;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.NEXT_QUEUE_UPDATED;
    }
}
