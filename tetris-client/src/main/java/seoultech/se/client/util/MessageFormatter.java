package seoultech.se.client.util;

/**
 * 게임 메시지 포맷팅 유틸리티
 * 
 * UI에 표시될 메시지를 일관된 형식으로 생성합니다.
 */
public class MessageFormatter {
    
    /**
     * 라인 클리어 타입 메시지를 생성합니다
     * 
     * @param linesCleared 클리어된 라인 수
     * @param isTSpin T-Spin 여부
     * @param isTSpinMini T-Spin Mini 여부
     * @return 포맷된 메시지 (예: "T-SPIN MINI SINGLE", "TETRIS")
     */
    public static String formatLineClearMessage(int linesCleared, boolean isTSpin, boolean isTSpinMini) {
        StringBuilder message = new StringBuilder();
        
        // T-Spin 표시
        if (isTSpin) {
            message.append(isTSpinMini ? "T-SPIN MINI " : "T-SPIN ");
        }
        
        // 라인 타입 표시
        switch (linesCleared) {
            case 1:
                message.append("SINGLE");
                break;
            case 2:
                message.append("DOUBLE");
                break;
            case 3:
                message.append("TRIPLE");
                break;
            case 4:
                message.append("TETRIS");
                break;
            default:
                // 0줄이거나 5줄 이상은 빈 문자열
                break;
        }
        
        return message.toString();
    }
    
    /**
     * 콤보 메시지를 생성합니다
     * 
     * @param comboCount 콤보 횟수
     * @return 포맷된 메시지 (예: "🔥 COMBO x3")
     */
    public static String formatComboMessage(int comboCount) {
        return "🔥 COMBO x" + comboCount;
    }
    
    /**
     * Back-to-Back 메시지를 생성합니다
     * 
     * @param b2bCount Back-to-Back 횟수
     * @return 포맷된 메시지 (예: "⚡ B2B x2")
     */
    public static String formatBackToBackMessage(int b2bCount) {
        return "⚡ B2B x" + b2bCount;
    }
    
    /**
     * 레벨 업 메시지를 생성합니다
     * 
     * @param newLevel 새로운 레벨
     * @return 포맷된 메시지 (예: "📈 LEVEL UP! - Level 5")
     */
    public static String formatLevelUpMessage(int newLevel) {
        return "📈 LEVEL UP! - Level " + newLevel;
    }
}
