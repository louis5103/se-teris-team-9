package seoultech.se.client.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import seoultech.se.client.model.Setting;
import seoultech.se.client.repository.SettingsRepository;
import seoultech.se.client.service.NavigationService;

@Component
public class CustomSettingSceneController extends BaseController {

    @Autowired
    private NavigationService navigationService;

    @Autowired
    private SettingsRepository settingsRepository;

    @FXML
    private VBox settingContainer;
    @FXML
    private Button backButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    private List<Setting> settings = new ArrayList<>();
    private Setting selectedSetting = null;

    @FXML
    public void initialize() {
        super.initialize();
        loadSettings();
    }

    private void loadSettings() {
        settings = settingsRepository.loadSettings();
        settingContainer.getChildren().clear();
        
        for (Setting setting : settings) {
            Button button = createSettingButton(setting);
            if (setting.isSelected()) {
                button.getStyleClass().add("custom-setting-button-selected");
                selectedSetting = setting;
            }
            settingContainer.getChildren().add(button);
        }
    }

    public void addSetting(Setting setting) {
        settings.add(setting);
        settingsRepository.saveSettings(settings);
        Button button = createSettingButton(setting);
        settingContainer.getChildren().add(button);
    }

    private Button createSettingButton(Setting setting) {
        Button button = new Button(setting.getName());
        button.getStyleClass().add("menu-button-middle");
        button.setMaxWidth(Double.MAX_VALUE);
        
        button.setOnAction(event -> {
            // Deselect all settings first
            settings.forEach(s -> s.setSelected(false));
            
            settingContainer.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    node.getStyleClass().remove("custom-setting-button-selected");
                }
            });

            // Select current setting
            setting.setSelected(true);
            button.getStyleClass().add("custom-setting-button-selected");
            selectedSetting = setting;
            
            // Save the changes
            settingsRepository.saveSettings(settings);
        });

        return button;
    }

    @FXML
    private void handleTestSettingButton(){
        
    }

    @FXML
    private void handleBackButton(ActionEvent event) throws IOException {
        navigationService.navigateTo("/view/setting-view.fxml");
    }

    @FXML
    private void handleSaveButton() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/custom-setting-pop.fxml"));
            Parent root = loader.load();
            
            Stage popupStage = new Stage();
            popupStage.initModality(Modality.APPLICATION_MODAL);
            popupStage.setTitle("Save Setting");
            popupStage.setScene(new Scene(root));
            
            CustomSettingPopController controller = loader.getController();
            controller.setMainController(this);
            
            popupStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteButton(){
        if (selectedSetting != null){
            settings.remove(selectedSetting);
            settingsRepository.saveSettings(settings);
            loadSettings();
            settingContainer.getChildren().forEach(node -> {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    btn.getStyleClass().remove("custom-setting-button-selected");
                }
            });
            selectedSetting = null;
        }
    }

    public void selectSetting(Setting setting) {
        // Deselect all settings
        settings.forEach(s -> s.setSelected(false));
        
        // Select the chosen setting
        setting.setSelected(true);
        settingsRepository.saveSettings(settings);
        
        // Update UI
        settingContainer.getChildren().forEach(node -> {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.getStyleClass().remove("custom-setting-button-selected");
                if (btn.getText().equals(setting.getName())) {
                    btn.getStyleClass().add("custom-setting-button-selected");
                }
            }
        });
    }
}
