package seoultech.se.core.event;

import lombok.Getter;

/**
 * 게임이 재개되었을 때 발생하는 이벤트
 */
@Getter
public class GameResumedEvent implements GameEvent {
    private final long timestamp;
    
    public GameResumedEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.GAME_RESUMED;
    }
}
