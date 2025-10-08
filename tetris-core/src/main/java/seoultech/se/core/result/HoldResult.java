package seoultech.se.core.result;

import lombok.Getter;
import seoultech.se.core.GameState;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * Hold 동작의 결과를 담는 클래스
 * 
 * Hold 동작은 다음 두 가지 경우가 있습니다:
 * 1. 성공: 현재 블록을 Hold하고 새 블록 생성 (또는 Hold된 블록 교체)
 * 2. 실패: 이미 이번 턴에 Hold를 사용했음
 */
@Getter
public class HoldResult {
    private final boolean success;
    private final GameState newState;
    private final TetrominoType previousHeldPiece;  // Hold 전에 보관되어 있던 블록 (없으면 null)
    private final TetrominoType newHeldPiece;       // 새로 Hold된 블록
    private final String failureReason;
    
    /**
     * 성공한 Hold 결과 생성
     */
    public static HoldResult success(GameState newState, TetrominoType previousHeld, TetrominoType newHeld) {
        return new HoldResult(true, newState, previousHeld, newHeld, null);
    }
    
    /**
     * 실패한 Hold 결과 생성
     */
    public static HoldResult failure(String reason) {
        return new HoldResult(false, null, null, null, reason);
    }
    
    private HoldResult(boolean success, GameState newState, 
                      TetrominoType previousHeld, TetrominoType newHeld, 
                      String failureReason) {
        this.success = success;
        this.newState = newState;
        this.previousHeldPiece = previousHeld;
        this.newHeldPiece = newHeld;
        this.failureReason = failureReason;
    }
}
