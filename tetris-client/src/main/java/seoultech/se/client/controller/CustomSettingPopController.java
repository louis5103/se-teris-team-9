package seoultech.se.client.controller;

import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seoultech.se.client.model.Setting;

import org.springframework.context.annotation.ComponentScans;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import seoultech.se.client.config.ApplicationContextProvider;
import seoultech.se.client.service.SettingsService;


@Component
public class CustomSettingPopController extends BaseController {
    private CustomSettingSceneController mainController;

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

    public void setMainController(CustomSettingSceneController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleSaveButton() {
        String settingName = settingNameField.getText().trim();
        if (!settingName.isEmpty()) {
            Setting newSetting = new Setting(settingName);
            Map<String, String> configurations = new HashMap<>();

            // TODO : results in null pointer exception
            try{
                // Get current settings from SettingsService
                configurations.put("soundVolume", String.valueOf(settingsService.soundVolumeProperty().getValue()));
                configurations.put("colorMode", settingsService.colorModeProperty().getValue());
                configurations.put("screenSize", settingsService.screenSizeProperty().getValue());
                configurations.put("stageWidth", String.valueOf(settingsService.stageWidthProperty().getValue()));
                configurations.put("stageHeight", String.valueOf(settingsService.stageHeightProperty().getValue()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            newSetting.setConfigurations(configurations);
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
