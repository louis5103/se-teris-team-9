package seoultech.se.client.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seoultech.se.client.config.ApplicationContextProvider;
import seoultech.se.client.model.Setting;


@Component
public class CustomSettingPopController extends BaseController {
    private SettingSceneController mainController;

    @FXML
    private TextField settingNameField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    @Override
    @FXML
    public void initialize() {
        super.initialize();
        this.settingsService = ApplicationContextProvider.getApplicationContext().getBean(seoultech.se.client.service.SettingsService.class);
    }

    public void setMainController(SettingSceneController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleSaveButton() {
        String settingName = settingNameField.getText().trim();
        
        // ✅ 입력 검증
        if (settingName.isEmpty()) {
            showWarning("설정 이름 필요", "설정 이름을 입력하세요.");
            return;
        }
        
        // ✅ 서비스 검증
        if (settingsService == null) {
            showError("서비스 오류", "SettingsService가 초기화되지 않았습니다.");
            return;
        }
        
        // ✅ mainController 검증
        if (mainController == null) {
            showError("컨트롤러 오류", "MainController가 설정되지 않았습니다.");
            return;
        }
        
        try {
            Setting newSetting = new Setting(settingName);
            Map<String, String> configurations = new HashMap<>();

            // ✅ Null-safe 설정 값 추출
            configurations.put("soundVolume", 
                settingsService.soundVolumeProperty().getValue() != null 
                    ? String.valueOf(settingsService.soundVolumeProperty().getValue()) 
                    : "50");
            
            configurations.put("colorMode", 
                settingsService.colorModeProperty().getValue() != null 
                    ? settingsService.colorModeProperty().getValue() 
                    : "NORMAL");
            
            configurations.put("screenSize", 
                settingsService.screenSizeProperty().getValue() != null 
                    ? settingsService.screenSizeProperty().getValue() 
                    : "MEDIUM");
            
            configurations.put("stageWidth", 
                settingsService.stageWidthProperty().getValue() != null 
                    ? String.valueOf(settingsService.stageWidthProperty().getValue()) 
                    : "800");
            
            configurations.put("stageHeight", 
                settingsService.stageHeightProperty().getValue() != null 
                    ? String.valueOf(settingsService.stageHeightProperty().getValue()) 
                    : "600");
            
            newSetting.setConfigurations(configurations);
            newSetting.setSelected(true); // Make this the active setting
            mainController.addCustomSetting(newSetting);
            newSetting.setSelected(true);
            mainController.addSetting(newSetting);
            
            showInfo("저장 완료", "설정 '" + settingName + "'이(가) 저장되었습니다.");
            closeWindow();
            
        } catch (NullPointerException e) {
            showError("NullPointerException", "설정 저장 중 오류가 발생했습니다: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showError("저장 실패", "설정 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCancelButton() {
        closeWindow();
    }

    @FXML
    private void handleInputAction() {
        
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
    
    // ========== UI 알림 메서드 ==========
    
    /**
     * 정보 알림 표시
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 경고 알림 표시
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * 오류 알림 표시
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

