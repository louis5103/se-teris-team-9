package seoultech.se.core.event;

import lombok.Getter;

/**
 * 콤보 중단 이벤트
 * 
 * 콤보가 끊겼을 때 발생합니다.
 */
@Getter
public class ComboBreakEvent implements GameEvent {
    private final int finalComboCount;
    private final long timestamp;

    public ComboBreakEvent(int finalComboCount) {
        this.finalComboCount = finalComboCount;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public EventType getType() {
        return EventType.COMBO_BREAK;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("ComboBreakEvent{finalComboCount=%d, timestamp=%d}", 
            finalComboCount, timestamp);
    }
}
