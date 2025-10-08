package seoultech.se.core.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 라인 클리어 이벤트
 * 
 * 이 Event는 하나 이상의 라인이 지워졌을 때 발생합니다.
 * 테트리스에서 가장 중요한 순간 중 하나입니다.
 * 
 * 라인 클리어의 종류:
 * 
 * 1. 일반 클리어:
 *    - Single: 1줄 지움
 *    - Double: 2줄 지움
 *    - Triple: 3줄 지움
 *    - Tetris: 4줄 지움 (I 블록으로만 가능)
 * 
 * 2. T-Spin:
 *    T 블록을 회전시켜서 어려운 위치에 끼워넣어 라인을 지우는 고급 테크닉입니다.
 *    일반 클리어보다 훨씬 높은 점수를 받습니다.
 *    
 *    - T-Spin Single: T 블록으로 1줄
 *    - T-Spin Double: T 블록으로 2줄
 *    - T-Spin Triple: T 블록으로 3줄
 *    - T-Spin Mini: 간단한 T-Spin (점수가 조금 낮음)
 * 
 * 3. Perfect Clear (PC):
 *    라인을 지운 후 보드가 완전히 비워진 상태입니다.
 *    매우 어렵지만 엄청난 점수 보너스를 받습니다.
 * 
 * 이 Event에 포함된 정보:
 * 
 * 1. linesCleared: 지워진 라인의 개수 (1~4)
 *    점수 계산과 레벨업에 사용됩니다.
 * 
 * 2. clearedRows: 지워진 라인의 실제 행 번호들
 *    UI에서 애니메이션 효과를 줄 때 사용합니다.
 *    예: [15, 16, 17, 18] - 아래쪽 4줄이 한꺼번에 지워짐
 * 
 * 3. isTSpin: T-Spin 여부
 *    T-Spin이면 특별한 사운드나 이펙트를 재생합니다.
 *    "T-SPIN!"이라는 큰 텍스트가 화면에 나타날 수도 있죠.
 * 
 * 4. isTSpinMini: Mini T-Spin 여부
 *    일반 T-Spin보다는 약하지만 여전히 보너스를 받습니다.
 * 
 * 5. isPerfectClear: Perfect Clear 여부
 *    매우 드문 상황이므로 특별한 연출이 필요합니다.
 *    번쩍이는 화면, 팡파레 사운드, "PERFECT CLEAR!!" 메시지 등
 * 
 * 점수 계산:
 * 
 * 라인 클리어의 점수는 다음 공식으로 계산됩니다:
 * 
 * 일반 클리어:
 * - Single: 100 × level
 * - Double: 300 × level
 * - Triple: 500 × level
 * - Tetris: 800 × level
 * 
 * T-Spin:
 * - T-Spin Mini Single: 200 × level
 * - T-Spin Single: 800 × level
 * - T-Spin Double: 1200 × level
 * - T-Spin Triple: 1600 × level
 * 
 * Back-to-Back 보너스:
 * Tetris나 T-Spin을 연속으로 하면 1.5배 추가 점수
 * 
 * Perfect Clear 보너스:
 * 라인 수에 따라 800~2000점 추가
 * 
 * UI에서의 처리:
 * 
 * GameController는 이 Event를 받으면 다음과 같이 처리합니다:
 * 
 * 1. 애니메이션 재생:
 *    clearedRows의 라인들을 하얗게 깜빡이게 하거나,
 *    폭발 이펙트를 재생합니다.
 * 
 * 2. 사운드 재생:
 *    일반 클리어: "뚜뚜뚜" 소리
 *    Tetris: "따라라란!" 팡파레
 *    T-Spin: "쨘!" 특수 효과음
 *    Perfect Clear: 화려한 팡파레
 * 
 * 3. 텍스트 표시:
 *    "SINGLE", "DOUBLE", "TRIPLE", "TETRIS!",
 *    "T-SPIN DOUBLE!", "PERFECT CLEAR!!" 등
 * 
 * 4. 통계 업데이트:
 *    총 라인 클리어 수, Tetris 횟수, T-Spin 횟수 등
 * 
 * 멀티플레이어에서의 사용:
 * 
 * 라인 클리어는 상대에게 공격을 보내는 주요 수단입니다:
 * - Single: 공격 없음 (오히려 취약)
 * - Double: 1줄 공격
 * - Triple: 2줄 공격
 * - Tetris: 4줄 공격
 * - T-Spin Double: 4줄 공격
 * - T-Spin Triple: 6줄 공격
 * 
 * 따라서 이 Event가 발생하면 AttackSentEvent도 함께 발생할 수 있습니다.
 * 
 * 애니메이션 타이밍:
 * 
 * 라인이 지워지는 과정은 시간이 걸립니다:
 * 1. 라인이 완성됨 (0ms)
 * 2. 깜빡임 애니메이션 (200ms)
 * 3. 라인 사라짐 (0ms)
 * 4. 위 블록들이 떨어짐 (300ms)
 * 5. 새 블록 생성 (0ms)
 * 
 * 총 약 500ms가 소요됩니다. 이 동안 사용자 입력은 무시되거나,
 * 큐에 저장되었다가 애니메이션이 끝난 후 처리됩니다.
 * 
 * 하지만 현대 테트리스에서는 애니메이션을 OFF로 하고 즉시 처리하는 것이 일반적입니다.
 * 빠른 속도의 게임에서는 애니메이션을 기다릴 시간이 없으니까요.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor  // JSON 역직렬화를 위해 필요
public class LineClearedEvent implements GameEvent {
    /**
     * 지워진 라인의 개수 (1~4)
     */
    private int linesCleared;
    
    /**
     * 지워진 라인의 행 번호 배열
     * 예: [18, 19] - 아래쪽 2줄이 지워짐
     */
    private int[] clearedRows;
    
    /**
     * T-Spin 여부
     * T 블록을 회전시켜서 라인을 지운 경우 true
     */
    private boolean isTSpin;
    
    /**
     * T-Spin Mini 여부
     * 간단한 T-Spin인 경우 true
     */
    private boolean isTSpinMini;
    
    /**
     * Perfect Clear 여부
     * 라인을 지운 후 보드가 완전히 비워진 경우 true
     */
    private boolean isPerfectClear;
    
    /**
     * Event 발생 시각 (밀리초)
     */
    private long timestamp;
    
    /**
     * 편의 생성자: 현재 시각을 자동으로 설정
     */
    public LineClearedEvent(int linesCleared, int[] clearedRows,
                           boolean isTSpin, boolean isTSpinMini, boolean isPerfectClear) {
        this.linesCleared = linesCleared;
        this.clearedRows = clearedRows;
        this.isTSpin = isTSpin;
        this.isTSpinMini = isTSpinMini;
        this.isPerfectClear = isPerfectClear;
        this.timestamp = System.currentTimeMillis();
    }
    
    @Override
    public EventType getType() {
        return EventType.LINE_CLEARED;
    }
    
    @Override
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        
        // 클리어 타입 결정
        if (isPerfectClear) {
            sb.append("PERFECT CLEAR! ");
        }
        
        if (isTSpin) {
            sb.append("T-SPIN ");
            if (isTSpinMini) {
                sb.append("MINI ");
            }
        }
        
        // 라인 수에 따른 이름
        switch (linesCleared) {
            case 1: sb.append("SINGLE"); break;
            case 2: sb.append("DOUBLE"); break;
            case 3: sb.append("TRIPLE"); break;
            case 4: sb.append("TETRIS"); break;
            default: sb.append(linesCleared).append(" LINES");
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("LineClearedEvent{lines=%d, tSpin=%b, perfectClear=%b, timestamp=%d}",
                           linesCleared, isTSpin, isPerfectClear, timestamp);
    }
}
