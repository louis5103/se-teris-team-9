package seoultech.se.backend.Score;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scores")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class ScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer score;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime updatedAt;

    public static ScoreEntity from(ScoreRequestDto score) {
        ScoreEntity entity = new ScoreEntity();
        entity.name = score.getName();
        entity.score = score.getScore();
        entity.updatedAt = score.getUpdatedAt();
        return entity;
    }
}
