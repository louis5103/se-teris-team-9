package seoultech.se.backend.score;

import java.time.LocalDateTime;

public interface ScoreRankDto {
    Integer getRank();
    String getName();
    Integer getScore();
    GameMode getGameMode();
    LocalDateTime getCreatedAt();
}
