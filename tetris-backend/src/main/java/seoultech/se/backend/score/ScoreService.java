package seoultech.se.backend.score;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/*
 * 작성자: 문주성
 * 공부할 것: strema, map, ::new, toList
 */

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
