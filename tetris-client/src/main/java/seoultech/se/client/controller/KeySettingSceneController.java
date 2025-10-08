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
 * KeyMappingService를 사용하여 키 매핑을 설정하고 저장합니다.
 * 
 * 사용 흐름:
 * 1. 사용자가 변경하고 싶은 액션의 버튼 클릭 (예: "왼쪽 이동")
 * 2. 버튼이 활성화되고 "Press any key..." 표시
 * 3. 사용자가 키를 누름
 * 4. KeyMappingService에 매핑 저장
 * 5. 버튼 텍스트 업데이트 (예: "← LEFT")
 * 
 * 멀티플레이어 시나리오:
 * - 각 플레이어가 독립적인 키 설정 사용
 * - 로컬 Preferences에 저장됨
 * - 서버는 키 설정을 알 필요 없음 (Command만 받음)
 */
@Component
public class KeySettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;
    
    @Autowired
    private KeyMappingService keyMappingService;
    
    @FXML
    private Button leftButton;
    @FXML
    private Button rightButton;
    @FXML
    private Button downButton;
    @FXML
    private Button hardDropButton;
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
        updateButtonLabel(leftButton, GameAction.MOVE_LEFT, "왼쪽 이동");
        updateButtonLabel(rightButton, GameAction.MOVE_RIGHT, "오른쪽 이동");
        updateButtonLabel(downButton, GameAction.MOVE_DOWN, "아래 이동");
        updateButtonLabel(hardDropButton, GameAction.HARD_DROP, "하드 드롭");
        updateButtonLabel(rotateButton, GameAction.ROTATE_CLOCKWISE, "회전");
    }
    
    /**
     * 버튼 레이블 업데이트 (액션명 + 현재 키)
     */
    private void updateButtonLabel(Button button, GameAction action, String actionName) {
        keyMappingService.getKey(action).ifPresentOrElse(
            key -> button.setText(actionName + ": " + key.getName()),
            () -> button.setText(actionName + ": (없음)")
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
    private void handleHardDropButton() {
        startKeyCapture(GameAction.HARD_DROP, hardDropButton);
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
