package seoultech.se.backend.Score;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScoreResponseDto {

    private String name;
    private Integer score;
    private LocalDateTime updatedAt;

    public ScoreResponseDto(ScoreEntity entity) {
        this.name = entity.getName();

    }
}