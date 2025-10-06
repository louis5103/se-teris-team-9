package seoultech.se.backend.Score;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class ScoreRequestDto {

    private String name;
    private Integer score;
    private LocalDateTime updatedAt;
}
