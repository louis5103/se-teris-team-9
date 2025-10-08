package seoultech.se.client.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seoultech.se.client.TetrisApplication;

@Service
public class NavigationService {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private SettingsService settingsService;

    public void navigateTo(String fxmlPath) throws IOException {
        Stage stage = settingsService.getPrimaryStage();
        FXMLLoader loader = new FXMLLoader(TetrisApplication.class.getResource(fxmlPath));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        
        // 게임 화면은 더 큰 크기가 필요하므로 sizeToScene() 사용
        if (fxmlPath.contains("game-view")) {
            stage.sizeToScene();
        } else {
            // 다른 화면들은 기본 크기 유지
            stage.setWidth(settingsService.stageWidthProperty().get());
            stage.setHeight(settingsService.stageHeightProperty().get());
        }
        
        stage.centerOnScreen();
        stage.show();
    }
}
