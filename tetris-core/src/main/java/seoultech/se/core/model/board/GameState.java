package seoultech.se.core.model.board;

import lombok.Getter;

@Getter
public class GameState {

    private long score = 0;
    private int linesCleared = 0;
    private int level = 1;
    private boolean isGameOver = false;

    public void addScore(long points) {
        this.score += points;
    }

    public void addLinesCleared(int count) {
        this.linesCleared += count;
        // 10줄을 클리어할 때마다 레벨업
        this.level = (this.linesCleared / 10) + 1;
    }

    public void setGameOver(boolean isGameOver) {
        this.isGameOver = isGameOver;
    }
}
