package seoultech.se.core.event;

import lombok.Getter;

/**
 * Back-to-Back 이벤트
 * 
 * 연속으로 어려운 클리어(Tetris, T-Spin 등)를 했을 때 발생합니다.
 */
@Getter
public class BackToBackEvent implements GameEvent {
    private final int backToBackCount;
    private final long timestamp;

    public BackToBackEvent(int backToBackCount) {
        this.backToBackCount = backToBackCount;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public EventType getType() {
        return EventType.BACK_TO_BACK;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("BackToBackEvent{backToBackCount=%d, timestamp=%d}", 
            backToBackCount, timestamp);
    }
}
