package seoultech.se.core.model.board.engine.result;

import lombok.Value;
import seoultech.se.core.model.board.GameState;

/**
 * 테트로미노 고정(Lock)의 단계
    * 1. 테트로미노를 grid에 추가
    * 2. 게임 오버 체크
    * 3. 라인 클리어 체크
    * 4. 점수 계산
 */
@Value
public class LockResult {
    boolean gameOver;
    GameState newState;
    LineClearResult lineClearResult;
    String gameOverReason;

    public static LockResult success(GameState newState, LineClearResult lineClearResult){
        return new LockResult(false, newState, lineClearResult, null);
    }

    public static LockResult gameOver(GameState newState, String reason) {
        return new LockResult(true, newState, LineClearResult.none(), reason);
    }
}
