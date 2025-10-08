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
        
        // 창 크기 변경 전 현재 위치와 크기 저장
        double currentX = stage.getX();
        double currentY = stage.getY();
        double currentWidth = stage.getWidth();
        double currentHeight = stage.getHeight();
        
        FXMLLoader loader = new FXMLLoader(TetrisApplication.class.getResource(fxmlPath));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root, settingsService.stageWidthProperty().get(), settingsService.stageHeightProperty().get());
        stage.setScene(scene);
        stage.setResizable(false);  // 창 크기 조절 불가 유지
        
        // 새 Scene 크기 가져오기
        stage.sizeToScene();  // Scene 크기에 맞게 Stage 크기 조정
        double newWidth = stage.getWidth();
        double newHeight = stage.getHeight();
        
        // 중앙 위치 유지: 크기 변화만큼 위치를 조정하여 중심점 유지
        double deltaX = (newWidth - currentWidth) / 2;
        double deltaY = (newHeight - currentHeight) / 2;
        stage.setX(currentX - deltaX);
        stage.setY(currentY - deltaY);
        
        stage.show();
    }
}
