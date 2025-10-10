package seoultech.se.backend.Score;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScoreResponseDto {

    private String name;
    private int score;
    private GameMode gameMode;
    private boolean isItemMode;

    public ScoreResponseDto(ScoreEntity entity) {
        this.name = entity.getName();
        this.score = entity.getScore();
        this.gameMode = entity.getGameMode();
        this.isItemMode = entity.isItemMode();
    }
}