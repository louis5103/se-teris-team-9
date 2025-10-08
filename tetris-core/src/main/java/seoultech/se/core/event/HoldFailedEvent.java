package seoultech.se.core.event;

import lombok.Getter;

/**
 * Hold 실패 시 발생하는 이벤트 (이미 이번 턴에 사용함)
 */
@Getter
public class HoldFailedEvent implements GameEvent {
    private final String reason;
    private final long timestamp;
    
    public HoldFailedEvent(String reason) {
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.HOLD_FAILED;
    }
}
