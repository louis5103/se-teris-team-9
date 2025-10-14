package seoultech.se.core.event;

import lombok.Value;

/**
 * 레벨업 이벤트
 * 
 * 플레이어가 충분한 라인을 클리어하여 레벨이 올랐을 때 발생합니다.
 * 
 * 레벨 시스템:
 * - 레벨 1 → 2: 10라인 필요
 * - 레벨 2 → 3: 20라인 필요 (누적)
 * - 레벨 3 → 4: 30라인 필요 (누적)
 * - ...
 * - 최대 레벨: 15
 * 
 * 레벨이 올라가면:
 * - 블록 낙하 속도가 빨라집니다
 * - 점수 배율이 증가합니다
 * - 난이도가 높아집니다
 * 
 * 사용 예시:
 * <pre>
 * if (leveledUp) {
 *     events.add(new LevelUpEvent(newLevel));
 * }
 * </pre>
 * 
 * Observer는 이 이벤트를 받아서:
 * - UI에 레벨업 알림 표시
 * - 게임 속도 조정
 * - 축하 효과 재생
 * 등의 작업을 수행할 수 있습니다.
 */
@Value
public class LevelUpEvent implements GameEvent {
    /**
     * 새로운 레벨 (레벨업 후의 레벨)
     */
    int newLevel;
    
    @Override
    public EventType getType() {
        return EventType.LEVEL_UP;
    }
    
    @Override
    public String getDescription() {
        return "Level up to " + newLevel;
    }
}
