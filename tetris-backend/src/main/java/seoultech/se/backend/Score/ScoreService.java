package seoultech.se.backend.Score;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreResponseDto saveNewScore(ScoreRequestDto userScore) {
        ScoreEntity newData = ScoreEntity.from(userScore);
        ScoreEntity savedData = scoreRepository.save(newData);
        return new ScoreResponseDto(savedData);
    }

    public List<ScoreResponseDto> getScoreBoard() {
        List<ScoreEntity> scoreList = scoreRepository.findAll();
        return scoreList.stream()
                .map(ScoreResponseDto::new)
                .collect(Collectors.toList());
    }

    public void deleteScoreBoard() {
        scoreRepository.deleteAll();
    }
    
}
