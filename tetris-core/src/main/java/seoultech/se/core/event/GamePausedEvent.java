package seoultech.se.core.event;

import lombok.Getter;

/**
 * 게임이 일시정지되었을 때 발생하는 이벤트
 */
@Getter
public class GamePausedEvent implements GameEvent {
    private final long timestamp;
    
    public GamePausedEvent() {
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.GAME_PAUSED;
    }
}
