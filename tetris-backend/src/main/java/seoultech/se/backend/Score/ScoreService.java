package seoultech.se.backend.Score;

import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScoreService {

    private final ScoreRepository scoreRepository;

    public ScoreResponseDto saveScore(ScoreRequestDto userScore) {
        ScoreEntity newData = userScore.toEntity();
        ScoreEntity savedData = scoreRepository.save(newData);
        return new ScoreResponseDto(savedData);
    }

    public List<ScoreResponseDto> getScoreBoard() {
        List<ScoreEntity> scoreList = scoreRepository.findAll();
        return scoreList.stream().map(ScoreResponseDto::new).toList();
    }

    public void deleteScoreBoard() {
        scoreRepository.deleteAll();
    }
    
}
