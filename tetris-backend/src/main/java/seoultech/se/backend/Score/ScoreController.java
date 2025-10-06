package seoultech.se.backend.Score;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    // player의 점수 기록
    @PostMapping
    public ResponseEntity<ScoreResponseDto> saveNewScore(@RequestBody ScoreRequestDto newScore) {
        ScoreResponseDto responseDto = scoreService.saveNewScore(newScore);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    // 점수 목록 가져오기
    @GetMapping
    public ResponseEntity<List<ScoreResponseDto>> getScoreBoard() {
        List<ScoreResponseDto> scoreBoard = scoreService.getScoreBoard();
        return ResponseEntity.ok(scoreBoard);
    }

    // 점수 목록 초기화하기
    @DeleteMapping
    public String deleteScoreBoard() {
        scoreService.deleteScoreBoard();
        return "Delete Complete";
    }

}