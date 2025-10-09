package seoultech.se.core.event;

import lombok.Getter;

/**
 * 콤보 이벤트
 * 
 * 연속으로 라인을 지웠을 때 발생합니다.
 */
@Getter
public class ComboEvent implements GameEvent {
    private final int comboCount;
    private final long timestamp;

    public ComboEvent(int comboCount) {
        this.comboCount = comboCount;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public EventType getType() {
        return EventType.COMBO;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("ComboEvent{comboCount=%d, timestamp=%d}", 
            comboCount, timestamp);
    }
}
