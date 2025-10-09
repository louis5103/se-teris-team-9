package seoultech.se.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import seoultech.se.client.model.GameAction;
import seoultech.se.client.service.KeyMappingService;
import seoultech.se.client.service.NavigationService;

/**
 * 키 설정 화면 컨트롤러
 * 
 * 사용자가 게임 조작 키를 커스터마이징할 수 있는 화면입니다.
 */
@Component
public class KeySettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;
    
    @Autowired
    private KeyMappingService keyMappingService;
    
    @FXML
    private javafx.scene.layout.BorderPane rootPane;
    
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private Button downButton;
    @FXML
    private Button floorButton;
    @FXML
    private Button rotateButton;
    @FXML
    private Button backButton;
    @FXML
    private Button resetButton;

    // 현재 키 입력을 기다리고 있는 액션
    private GameAction waitingForKey = null;
    private Button activeButton = null;

    @FXML
    public void initialize() {
        super.initialize();
        updateButtonLabels();
    }
    
    /**
     * 모든 버튼의 레이블을 현재 키 매핑으로 업데이트
     */
    private void updateButtonLabels() {
        updateButtonLabel(leftButton, GameAction.MOVE_LEFT, "Left");
        updateButtonLabel(rightButton, GameAction.MOVE_RIGHT, "Right");
        updateButtonLabel(downButton, GameAction.MOVE_DOWN, "Down");
        updateButtonLabel(floorButton, GameAction.HARD_DROP, "Hard Drop");
        updateButtonLabel(rotateButton, GameAction.ROTATE_CLOCKWISE, "Rotate");
    }
    
    /**
     * 버튼 레이블 업데이트 (액션명 + 현재 키)
     */
    private void updateButtonLabel(Button button, GameAction action, String actionName) {
        keyMappingService.getKey(action).ifPresentOrElse(
            key -> button.setText(actionName + ": " + key.getName()),
            () -> button.setText(actionName + ": (NONE)")
        );
    }

    @FXML
    private void handleLeftButton() {
        startKeyCapture(GameAction.MOVE_LEFT, leftButton);
    }
    
    @FXML
    private void handleRightButton() {
        startKeyCapture(GameAction.MOVE_RIGHT, rightButton);
    }
    
    @FXML
    private void handleDownButton() {
        startKeyCapture(GameAction.MOVE_DOWN, downButton);
    }
    
    @FXML
    private void handleFloorButton() {
        startKeyCapture(GameAction.HARD_DROP, floorButton);
    }
    
    @FXML
    private void handleRotateButton() {
        startKeyCapture(GameAction.ROTATE_CLOCKWISE, rotateButton);
    }
    
    /**
     * 키 입력 대기 모드 시작
     */
    private void startKeyCapture(GameAction action, Button button) {
        waitingForKey = action;
        activeButton = button;
        button.setText("Press any key...");
        button.setStyle("-fx-background-color: #4CAF50;");
        
        // 키 입력 리스너 등록
        rootPane.setOnKeyPressed(this::handleKeyCaptured);
        rootPane.requestFocus();
    }
    
    /**
     * 키 입력 감지 및 매핑 저장
     */
    private void handleKeyCaptured(KeyEvent event) {
        if (waitingForKey == null) {
            return;
        }
        
        KeyCode key = event.getCode();
        
        // ESC는 취소
        if (key == KeyCode.ESCAPE) {
            cancelKeyCapture();
            return;
        }
        
        // 키 매핑 저장
        boolean success = keyMappingService.setKeyMapping(waitingForKey, key);
        
        if (success) {
            System.out.println("✅ Key mapped: " + waitingForKey + " → " + key);
            updateButtonLabels();
        } else {
            System.err.println("❌ Failed to map key: " + key);
        }
        
        cancelKeyCapture();
        event.consume();
    }
    
    /**
     * 키 입력 대기 취소
     */
    private void cancelKeyCapture() {
        if (activeButton != null) {
            activeButton.setStyle("");
        }
        waitingForKey = null;
        activeButton = null;
        rootPane.setOnKeyPressed(null);
        updateButtonLabels();
    }
    
    @FXML
    private void handleResetButton() {
        keyMappingService.resetToDefault();
        updateButtonLabels();
        System.out.println("🔄 Key mappings reset to default");
    }
    
    @FXML
    private void handleBackButton(ActionEvent event) throws Exception {
        cancelKeyCapture(); // 혹시 키 입력 대기 중이면 취소
        navigationService.navigateTo("/view/setting-view.fxml");
    }
}
