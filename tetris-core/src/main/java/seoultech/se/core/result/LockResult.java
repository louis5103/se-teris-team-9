package seoultech.se.core.result;

import lombok.Value;
import seoultech.se.core.GameState;
import seoultech.se.core.model.Tetromino;

/**
 * 테트로미노 고정(Lock)의 단계
    * 1. 테트로미노를 grid에 추가
    * 2. 게임 오버 체크
    * 3. 라인 클리어 체크
    * 4. 점수 계산
 * 
 * 이 Result는 고정된 블록의 정보를 포함합니다.
 * EventMapper가 TetrominoLockedEvent를 생성할 때 필요합니다.
 */
@Value
public class LockResult {
    boolean gameOver;
    GameState newState;
    LineClearResult lineClearResult;
    String gameOverReason;
    
    // 고정된 블록 정보 (EventMapper에서 사용)
    Tetromino lockedTetromino;
    int lockedX;
    int lockedY;

    public static LockResult success(
            GameState newState, 
            LineClearResult lineClearResult,
            Tetromino lockedTetromino,
            int lockedX,
            int lockedY
    ) {
        return new LockResult(
            false, 
            newState, 
            lineClearResult, 
            null,
            lockedTetromino,
            lockedX,
            lockedY
        );
    }

    public static LockResult gameOver(
            GameState newState, 
            String reason,
            Tetromino lockedTetromino,
            int lockedX,
            int lockedY
    ) {
        return new LockResult(
            true, 
            newState, 
            LineClearResult.none(), 
            reason,
            lockedTetromino,
            lockedX,
            lockedY
        );
    }
}
