package seoultech.se.client.model.scoreBoard;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import seoultech.se.backend.score.ScoreRankDto;
import seoultech.se.backend.score.ScoreService;


@Getter
@Component
@RequiredArgsConstructor
public class ScoreBoard extends VBox{
    private final TableView<ScoreRankDto> tableView = new TableView<>();
    private final ScoreService scoreService;

    
    @PostConstruct
    public void init () {
        // 라벨
        Label title = new Label("🏆싱글모드 점수 순위");

        // 테이블 컬럼
        TableColumn<ScoreRankDto, String> rankCol = new TableColumn<>("순위");
        TableColumn<ScoreRankDto, String> nameCol = new TableColumn<>("이름");
        TableColumn<ScoreRankDto, Integer> scoreCol = new TableColumn<>("점수");
        TableColumn<ScoreRankDto, String> modeCol = new TableColumn<>("게임 모드");
        TableColumn<ScoreRankDto, String> dateCol = new TableColumn<>("날짜");

        // 컬럼 설정
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        modeCol.setCellValueFactory(new PropertyValueFactory<>("gameMode"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // 테이블에 컬럼 추가
        tableView.getColumns().addAll(rankCol, nameCol, scoreCol, modeCol, dateCol);

        // tetris-backend에서 실제 데이터 가져옴.
        ObservableList<ScoreRankDto> scores = loadSingleData();
        tableView.setItems(scores);
    }

    private ObservableList<ScoreRankDto> loadSingleData() {
        // 데이터 로드
        return FXCollections.observableArrayList(scoreService.getScoreRank(false, null));
    }
    
    private ObservableList<ScoreRankDto> loadAcadeData() {
        // 데이터 로드
        return FXCollections.observableArrayList(scoreService.getScoreRank(true, null));
    }

    public void updateDataWhenClicked(boolean isItemMode) {
        ObservableList<ScoreRankDto> scores = (isItemMode==true) ? loadAcadeData() : loadSingleData();
        tableView.setItems(scores);
    }
}
