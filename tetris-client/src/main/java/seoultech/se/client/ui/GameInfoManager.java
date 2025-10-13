package seoultech.se.client.ui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import seoultech.se.core.GameState;

/**
 * 게임 정보 레이블을 관리하는 클래스
 * 
 * 이 클래스는 다음과 같은 게임 정보를 표시하고 관리합니다:
 * - 점수 (Score)
 * - 레벨 (Level)
 * - 클리어한 라인 수 (Lines)
 * 
 * GameController에서 게임 정보 레이블 업데이트 책임을 분리하여
 * 단일 책임 원칙(SRP)을 준수합니다.
 */
public class GameInfoManager {
    
    // UI 요소들
    private final Label scoreLabel;
    private final Label levelLabel;
    private final Label linesLabel;
    
    /**
     * GameInfoManager 생성자
     * 
     * @param scoreLabel 점수 레이블
     * @param levelLabel 레벨 레이블
     * @param linesLabel 라인 수 레이블
     */
    public GameInfoManager(Label scoreLabel, Label levelLabel, Label linesLabel) {
        this.scoreLabel = scoreLabel;
        this.levelLabel = levelLabel;
        this.linesLabel = linesLabel;
    }
    
    /**
     * 게임 상태로부터 모든 정보 레이블을 업데이트합니다
     * 
     * @param gameState 현재 게임 상태
     */
    public void updateAll(GameState gameState) {
        Platform.runLater(() -> {
            updateScore(gameState.getScore());
            updateLevel(gameState.getLevel());
            updateLines(gameState.getLinesCleared());
        });
    }
    
    /**
     * 점수 레이블을 업데이트합니다
     * 
     * @param score 점수
     */
    public void updateScore(long score) {
        Platform.runLater(() -> {
            scoreLabel.setText(String.valueOf(score));
        });
    }
    
    /**
     * 레벨 레이블을 업데이트합니다
     * 
     * @param level 레벨
     */
    public void updateLevel(int level) {
        Platform.runLater(() -> {
            levelLabel.setText(String.valueOf(level));
        });
    }
    
    /**
     * 라인 수 레이블을 업데이트합니다
     * 
     * @param lines 클리어한 라인 수
     */
    public void updateLines(int lines) {
        Platform.runLater(() -> {
            linesLabel.setText(String.valueOf(lines));
        });
    }
    
    /**
     * 모든 레이블을 초기화합니다 (0으로 설정)
     */
    public void reset() {
        Platform.runLater(() -> {
            scoreLabel.setText("0");
            levelLabel.setText("1");
            linesLabel.setText("0");
        });
    }
    
    /**
     * 특정 값으로 모든 레이블을 설정합니다
     * 
     * @param score 점수
     * @param level 레벨
     * @param lines 라인 수
     */
    public void setAll(long score, int level, int lines) {
        Platform.runLater(() -> {
            scoreLabel.setText(String.valueOf(score));
            levelLabel.setText(String.valueOf(level));
            linesLabel.setText(String.valueOf(lines));
        });
    }
}
