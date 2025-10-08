package seoultech.se.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 점수 추가 이벤트
 * 
 * 이 Event는 플레이어가 점수를 획득했을 때 발생합니다.
 * 
 * 점수를 얻는 방법:
 * 
 * 1. 라인 클리어: 가장 주된 점수 획득 방법
 *    - Single부터 Tetris, T-Spin까지 다양한 점수
 *    - 레벨에 비례하여 점수가 증가
 *    - Back-to-Back 보너스로 1.5배
 * 
 * 2. Soft Drop: DOWN 키를 눌러 블록을 빠르게 내릴 때
 *    - 한 칸당 1점
 *    - 계속 누르고 있으면 점수가 쌓임
 * 
 * 3. Hard Drop: 스페이스바로 블록을 즉시 떨어뜨릴 때
 *    - 한 칸당 2점
 *    - 20칸을 떨어뜨렸다면 40점
 * 
 * 4. 콤보 보너스: 연속으로 라인을 지울 때
 *    - 콤보 수 × 50 × 레벨
 *    - 콤보가 길수록 점수가 빠르게 불어남
 * 
 * 5. Perfect Clear 보너스: 보드를 완전히 비웠을 때
 *    - 라인 수에 따라 800~2000점 추가
 * 
 * 이 Event에 포함된 정보:
 * 
 * 1. points: 추가된 점수
 *    실제로 GameState의 score에 더해질 값입니다.
 * 
 * 2. reason: 점수를 얻은 이유
 *    "SINGLE", "DOUBLE", "TETRIS", "T-SPIN DOUBLE",
 *    "HARD_DROP", "SOFT_DROP", "COMBO", "PERFECT_CLEAR" 등
 *    
 *    이것은 UI에서 표시할 텍스트로 사용됩니다.
 *    예: "+800 TETRIS!", "+40 HARD DROP"
 * 
 * UI에서의 처리:
 * 
 * 점수가 추가되면 다음과 같은 효과를 줄 수 있습니다:
 * 
 * 1. 점수 카운터 애니메이션:
 *    현재 점수에서 새 점수로 부드럽게 증가하는 애니메이션
 *    예: 1000 → 1800 (숫자가 빠르게 올라감)
 * 
 * 2. 획득 점수 팝업:
 *    "+800"이라는 텍스트가 화면 중앙에 크게 나타났다가 사라짐
 *    큰 점수일수록 글자가 크고 화려함
 * 
 * 3. 이유 표시:
 *    "TETRIS!", "T-SPIN DOUBLE!" 같은 텍스트
 *    점수와 함께 또는 따로 표시
 * 
 * 4. 시각 효과:
 *    큰 점수를 얻으면 화면이 흔들리거나 번쩍임
 *    Perfect Clear는 특히 화려한 효과
 * 
 * 5. 사운드:
 *    "띵!" 소리나 동전 소리 같은 피드백
 * 
 * 점수 표시의 심리학:
 * 
 * 좋은 게임 UI는 플레이어에게 성취감을 줍니다.
 * 점수를 얻었을 때 화려한 피드백을 주면, 플레이어는 더 열심히 플레이하게 됩니다.
 * 
 * 예를 들어:
 * - 작은 점수: 간단한 "띵" 소리와 작은 텍스트
 * - 중간 점수: 더 크고 화려한 텍스트, 약간의 화면 흔들림
 * - 큰 점수: 화면 전체에 걸친 효과, 팡파레 음악, 느린 모션
 * 
 * 이런 차등적인 피드백이 플레이어의 몰입감을 높입니다.
 * 단순히 점수 숫자만 올라가는 것보다 훨씬 재미있죠.
 * 
 * 점수 계산의 일관성:
 * 
 * 점수는 GameEngine에서 계산되어 LockResult나 다른 Result에 포함됩니다.
 * SessionManager가 이것을 ScoreAddedEvent로 변환합니다.
 * 
 * 중요한 것은 점수 계산 로직이 한 곳(GameEngine)에만 있다는 점입니다.
 * UI나 Service에서 점수를 계산하면 안 됩니다. 그러면 버그가 생기기 쉽고,
 * 멀티플레이어에서 클라이언트와 서버의 점수가 달라질 수 있습니다.
 * 
 * 모든 클라이언트는 서버가 보내는 ScoreAddedEvent를 신뢰해야 합니다.
 * 자기가 계산한 점수와 다르더라도, 서버의 판정이 최종입니다.
 * 
 * 실시간 피드백과 서버 검증의 균형:
 * 
 * 클라이언트는 즉각적인 피드백을 위해 로컬에서 점수를 예측할 수 있습니다.
 * (Client Prediction)
 * 
 * 하지만 서버로부터 정확한 점수를 받으면 그것으로 보정합니다.
 * (Server Reconciliation)
 * 
 * 대부분의 경우 예측이 정확하므로 플레이어는 이를 느끼지 못합니다.
 * 하지만 치팅이나 네트워크 오류로 점수가 달라지면, 서버 값으로 수정됩니다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor  // JSON 역직렬화를 위해 필요
public class ScoreAddedEvent implements GameEvent {
    /**
     * 추가된 점수
     */
    private long points;
    
    /**
     * 점수를 얻은 이유
     * 예: "SINGLE", "TETRIS", "T-SPIN DOUBLE", "HARD_DROP", "PERFECT_CLEAR"
     */
    private String reason;
    
    /**
     * Event 발생 시각 (밀리초)
     */
    private long timestamp;
    
    /**
     * 편의 생성자: 현재 시각을 자동으로 설정
     */
    public ScoreAddedEvent(long points, String reason) {
        this.points = points;
        this.reason = reason;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.SCORE_ADDED;
    }
    
    @Override
    public String getDescription() {
        return String.format("+%d points (%s)", points, reason);
    }
    
    @Override
    public String toString() {
        return String.format("ScoreAddedEvent{points=%d, reason='%s', timestamp=%d}",
                           points, reason, timestamp);
    }
}
