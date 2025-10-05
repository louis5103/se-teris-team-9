package seoultech.se.backend.Score;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping
    public ResponseEntity<List<ScoreResponseDto>>getScoreBoard() {
        List<ScoreResponseDto> scoreBoard = scoreService.getScoreBoard();
        return ResponseEntity.ok(scoreBoard);
    }

}