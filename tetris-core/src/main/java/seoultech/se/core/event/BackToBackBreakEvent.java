package seoultech.se.core.event;

import lombok.Getter;

/**
 * Back-to-Back 중단 이벤트
 * 
 * Back-to-Back 체인이 끊겼을 때 발생합니다.
 */
@Getter
public class BackToBackBreakEvent implements GameEvent {
    private final int finalBackToBackCount;
    private final long timestamp;

    public BackToBackBreakEvent(int finalBackToBackCount) {
        this.finalBackToBackCount = finalBackToBackCount;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public EventType getType() {
        return EventType.BACK_TO_BACK_BREAK;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("BackToBackBreakEvent{finalBackToBackCount=%d, timestamp=%d}", 
            finalBackToBackCount, timestamp);
    }
}
