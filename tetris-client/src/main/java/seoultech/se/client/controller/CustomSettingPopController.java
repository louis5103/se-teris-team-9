package seoultech.se.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seoultech.se.client.model.Setting;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CustomSettingPopController extends BaseController {
    private CustomSettingSceneController mainController;

    @FXML
    private TextField settingNameField;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    public void setMainController(CustomSettingSceneController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleSaveButton() {
        String settingName = settingNameField.getText().trim();
        if (!settingName.isEmpty()) {
            Setting newSetting = new Setting(settingName);
            newSetting.setSelected(true); // Make this the active setting
            mainController.addSetting(newSetting);
            closeWindow();
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
}
