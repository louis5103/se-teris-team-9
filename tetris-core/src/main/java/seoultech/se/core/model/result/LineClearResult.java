package seoultech.se.core.model.result;

import lombok.Value;

@Value
public class LineClearResult {
    int linesCleared;
    int[] clearedRows;
    boolean isTSpin;
    boolean isTSpinMini;
    boolean isPerfectClear;
    long scoreEarned;

    // 라인이 지워지지 않은 경우.
    public static LineClearResult none() {
        return new LineClearResult(0, new int[0], false, false, false, 0);
    }

    // 일반 라인 클리어
    public static LineClearResult normal(int lines, int[] rows, long score) {
        return new LineClearResult(lines, rows, false, false, false, score);
    }

    // T-Spin 라인 클리어
    public static LineClearResult tSpin(int lines, int[] rows, boolean mini, long score){
        return new LineClearResult(lines, rows, true, mini, false, score);
    }

    // 퍼펙트 클리어
    public static LineClearResult perfectClear(int lines, int[] rows, boolean tSpin, long score){
        return new LineClearResult(lines, rows, tSpin, false, true, score);
    }
}
