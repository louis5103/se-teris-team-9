package seoultech.se.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 게임 오버 이벤트
 * 
 * 이 Event는 게임이 종료되었을 때 발생합니다.
 * 테트리스에서 가장 슬픈 순간이자, 새로운 도전의 시작점입니다.
 * 
 * 게임 오버의 원인:
 * 
 * 1. Block Out (가장 일반적):
 *    새로운 블록이 spawn될 때, 이미 그 위치에 다른 블록이 있는 경우
 *    블록이 나타날 공간이 없어서 게임이 끝납니다.
 * 
 * 2. Lock Out:
 *    블록이 spawn 라인(보통 20번째 줄) 위에서 고정되는 경우
 *    현대 테트리스에서는 이것도 게임 오버로 간주합니다.
 * 
 * 3. Top Out:
 *    블록이 화면 위쪽(보이지 않는 영역)에 쌓이는 경우
 *    일부 테트리스 버전에서 사용하는 기준입니다.
 * 
 * 이 Event에 포함된 정보:
 * 
 * 1. reason: 게임 오버의 원인
 *    "BLOCK_OUT", "LOCK_OUT", "TOP_OUT" 등
 *    디버깅이나 통계 수집에 사용됩니다.
 * 
 * 2. finalScore: 최종 점수
 *    게임 오버 화면에 표시됩니다.
 *    이 점수로 순위표(Leaderboard)에 등록할 수 있습니다.
 * 
 * 3. finalLevel: 최종 레벨
 *    플레이어가 어느 레벨까지 도달했는지 보여줍니다.
 * 
 * 4. linesCleared: 지운 라인 수
 *    또 다른 성취 지표입니다.
 * 
 * 5. playTime: 플레이 시간 (초)
 *    얼마나 오래 버텼는지 측정합니다.
 * 
 * UI에서의 처리:
 * 
 * GameController는 이 Event를 받으면 다음과 같이 처리합니다:
 * 
 * 1. 게임 루프 중단:
 *    AnimationTimer를 멈춥니다.
 *    더 이상 블록이 떨어지지 않습니다.
 * 
 * 2. 입력 차단:
 *    모든 게임 플레이 입력을 무시합니다.
 *    오직 재시작이나 메뉴로 나가는 입력만 받습니다.
 * 
 * 3. Game Over 화면 표시:
 *    "GAME OVER" 텍스트와 함께 최종 통계를 보여줍니다:
 *    - 점수
 *    - 레벨
 *    - 라인 수
 *    - 플레이 시간
 *    - 최고 콤보
 *    - Tetris 횟수
 *    등등
 * 
 * 4. 사운드 재생:
 *    슬픈 음악이나 "Game Over" 보이스
 * 
 * 5. 리더보드 확인:
 *    최종 점수가 순위권에 드는지 확인합니다.
 *    새 기록이면 이름 입력 화면을 표시합니다.
 * 
 * 6. 옵션 제공:
 *    - Retry: 같은 모드로 다시 시작
 *    - Menu: 메인 메뉴로 돌아가기
 *    - Quit: 게임 종료
 * 
 * 좋은 Game Over 화면의 조건:
 * 
 * 1. 즉각적인 피드백:
 *    게임이 끝났다는 것을 명확하게 알립니다.
 *    하지만 갑자기 나타나면 당황스러우므로,
 *    짧은 애니메이션(화면이 천천히 어두워짐)을 주는 것이 좋습니다.
 * 
 * 2. 성취 강조:
 *    단순히 "실패했다"가 아니라,
 *    "이만큼 잘했다"는 긍정적인 메시지를 줍니다.
 *    예: "Wow! Level 15 까지 도달했어요!"
 * 
 * 3. 비교 제공:
 *    이전 기록이나 평균과 비교하여 발전을 보여줍니다.
 *    예: "이전 최고 기록보다 2000점 높아요!"
 * 
 * 4. 재도전 유도:
 *    "한 번 더?" 버튼을 크고 눈에 띄게 만듭니다.
 *    ESC나 Space로 바로 재시작할 수 있게 합니다.
 * 
 * 멀티플레이어에서의 Game Over:
 * 
 * 멀티플레이어에서는 한 플레이어의 Game Over가 다른 플레이어에게 영향을 줍니다:
 * 
 * 1. 1 vs 1 모드:
 *    한 명이 Game Over되면 다른 플레이어가 승리합니다.
 *    "Player 2 Wins!" 메시지를 표시합니다.
 * 
 * 2. Battle Royale (여러 명):
 *    한 명씩 탈락하고, 마지막 남은 사람이 승리합니다.
 *    탈락 순서와 생존 시간을 기록합니다.
 * 
 * 3. 협동 모드:
 *    한 명이 Game Over되어도 다른 플레이어가 계속 플레이합니다.
 *    혹은 한 명이라도 Game Over되면 전체 팀이 패배합니다.
 * 
 * Game Over 후 데이터 저장:
 * 
 * 게임이 끝나면 다음 정보를 저장해야 합니다:
 * 
 * 1. 로컬 저장:
 *    최고 점수, 최고 레벨 등을 로컬 파일에 저장합니다.
 * 
 * 2. 서버 전송 (온라인 모드):
 *    게임 결과를 서버로 전송하여 글로벌 리더보드에 등록합니다.
 *    통계 정보(평균 점수, 플레이 횟수)도 업데이트합니다.
 * 
 * 3. 리플레이 저장 (선택적):
 *    전체 게임 과정을 Command 시퀀스로 저장하여,
 *    나중에 다시 볼 수 있게 합니다.
 * 
 * 치팅 방지:
 * 
 * 온라인 리더보드에서는 클라이언트가 보내는 점수를 그대로 믿으면 안 됩니다.
 * 치팅으로 무한 점수를 보낼 수 있으니까요.
 * 
 * 방법:
 * 1. 서버에서 점수를 계산: 클라이언트는 Command만 보냄
 * 2. 검증 가능한 증거 제공: 게임 과정을 녹화하여 함께 전송
 * 3. 이상 탐지: 비정상적으로 높은 점수는 수동 검토
 * 
 * 심리적 효과:
 * 
 * Game Over는 부정적인 경험이지만, 잘 디자인하면 긍정적으로 만들 수 있습니다:
 * 
 * 나쁜 예:
 * "GAME OVER - YOU FAILED"
 * (플레이어는 실패감을 느끼고 게임을 그만둡니다)
 * 
 * 좋은 예:
 * "Great Job! You cleared 150 lines!"
 * "Just 500 points to beat your record!"
 * (플레이어는 발전을 느끼고 다시 도전합니다)
 * 
 * 이런 작은 차이가 게임의 리텐션(retention)에 큰 영향을 줍니다.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor  // JSON 역직렬화를 위해 필요
public class GameOverEvent implements GameEvent {
    /**
     * 게임 오버의 원인
     * 예: "BLOCK_OUT", "LOCK_OUT", "TOP_OUT"
     */
    private String reason;
    
    /**
     * 최종 점수
     */
    private long finalScore;
    
    /**
     * 최종 레벨
     */
    private int finalLevel;
    
    /**
     * 지운 라인 수
     */
    private int linesCleared;
    
    /**
     * 플레이 시간 (초)
     */
    private long playTimeSeconds;
    
    /**
     * Event 발생 시각 (밀리초)
     */
    private long timestamp;
    
    /**
     * 편의 생성자: 현재 시각을 자동으로 설정
     */
    public GameOverEvent(String reason, long finalScore, int finalLevel, 
                        int linesCleared, long playTimeSeconds) {
        this.reason = reason;
        this.finalScore = finalScore;
        this.finalLevel = finalLevel;
        this.linesCleared = linesCleared;
        this.playTimeSeconds = playTimeSeconds;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.GAME_OVER;
    }
    
    @Override
    public String getDescription() {
        return String.format("Game Over (%s) - Score: %d, Level: %d, Lines: %d",
                           reason, finalScore, finalLevel, linesCleared);
    }
    
    @Override
    public String toString() {
        return String.format("GameOverEvent{reason='%s', score=%d, level=%d, lines=%d, time=%ds}",
                           reason, finalScore, finalLevel, linesCleared, playTimeSeconds);
    }
}
