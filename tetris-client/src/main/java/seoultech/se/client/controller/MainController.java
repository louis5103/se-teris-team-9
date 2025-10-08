package seoultech.se.client.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seoultech.se.backend.service.GameService;
import seoultech.se.client.TetrisApplication;
import seoultech.se.client.config.ApplicationContextProvider;
import seoultech.se.client.service.NavigationService;


/**
 * 🎮 JavaFX 메인 메뉴 컨트롤러 (Spring DI 통합)
 * 
 * JavaFX UI와 Spring Boot 서비스를 연결하는 컨트롤러
 * - @Component로 Spring DI 컨테이너에 등록
 * - @Autowired로 서비스 레이어 주입
 * - START 버튼을 누르면 게임 화면(game-view.fxml)으로 전환
 * 
 * 핵심 개념:
 * ApplicationContextProvider를 통해 Spring Context에 접근하여
 * 게임 화면의 Controller(GameController)를 Spring Bean으로 생성합니다.
 */
@Component
public class MainController extends BaseController {
    
    @Autowired
    private GameService gameService;

    @Autowired
    private NavigationService navigationService;
    
    /**
     * UI 초기화 메서드
     * FXML 파일이 로드된 후 자동으로 호출됩니다
     */
    public void initialize() {
        System.out.println("✅ MainController initialized with Spring DI");
        System.out.println("📊 Service Status: " + gameService.getStatus());
    }

    /**
     * 설정 버튼 액션
     */
    public void handleSettingsButtonAction(ActionEvent event) throws IOException {
        System.out.println("⚙️ Settings button clicked");
        navigationService.navigateTo("/view/setting-view.fxml");
    }

    /**
     * SCORE 버튼 액션 (향후 구현 예정)
     */
    public void handleScoreButtonAction() {
        System.out.println("🏆 Score button clicked");
        // TODO: 점수판 화면 구현
    }

    /**
     * START 버튼 액션 - 게임 화면으로 전환
     * 
     * 동작 원리를 차근차근 설명하자면:
     * 
     * 1. 버튼 클릭 이벤트에서 현재 창(Stage)을 가져옵니다
     *    - JavaFX에서 Stage는 창(Window)을 의미합니다
     *    - ActionEvent의 source에서 버튼을 찾고, 그 버튼이 있는 Scene을 찾고,
     *      그 Scene이 있는 Stage를 찾습니다 (마치 거슬러 올라가는 것처럼)
     * 
     * 2. game-view.fxml 파일을 로드합니다
     *    - FXMLLoader가 XML 파일을 읽어서 UI 객체들을 생성합니다
     *    - 이때 controllerFactory를 설정하여 Spring이 Controller를 생성하게 합니다
     * 
     * 3. 생성된 UI를 Scene에 담아서 Stage에 설정합니다
     *    - 마치 무대(Stage)의 배경(Scene)을 교체하는 것과 같습니다
     * 
     * 4. GameController의 initialize()가 자동으로 호출되며 게임이 시작됩니다
     */
    public void handleStartButtonAction(ActionEvent event) {
        System.out.println("▶️ Start button clicked - Loading game...");
        
        try {
            // 1단계: 현재 버튼이 있는 창(Stage) 가져오기
            // event.getSource() -> 클릭된 버튼
            // getScene() -> 버튼이 있는 Scene
            // getWindow() -> Scene이 표시되는 Window (Stage)
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            // 2단계: game-view.fxml 로드 준비
            // 클래스패스에서 리소스 파일을 찾습니다
            FXMLLoader loader = new FXMLLoader(
                TetrisApplication.class.getResource("/view/game-view.fxml")
            );
            
            // 3단계: Controller Factory 설정
            // FXML이 Controller를 필요로 할 때, 직접 만들지 않고
            // Spring에게 "이 클래스의 Bean을 주세요"라고 요청합니다
            // 이렇게 하면 GameController가 Spring Bean으로 생성되어
            // @Autowired 등의 의존성 주입이 작동합니다
            ApplicationContext context = ApplicationContextProvider.getApplicationContext();
            loader.setControllerFactory(context::getBean);
            
            // 4단계: FXML 파일을 실제로 로드하여 UI 객체 트리 생성
            // 이 과정에서 GameController의 인스턴스가 생성되고
            // @FXML로 표시된 필드들이 UI 요소와 연결됩니다
            Parent gameRoot = loader.load();
            
            // 5단계: 새로운 Scene 생성 및 Stage에 설정
            // Scene은 UI 요소들의 컨테이너이자 하나의 화면을 의미합니다
            Scene gameScene = new Scene(gameRoot);
            stage.setScene(gameScene);
            stage.setTitle("Tetris - Playing");
            
            // 6단계: 로드 완료!
            // GameController의 initialize() 메서드가 이미 호출되었고
            // 게임이 시작되었습니다
            System.out.println("✅ Game scene loaded successfully");
            System.out.println("🎮 Game is now running!");
            
        } catch (IOException e) {
            // FXML 파일을 찾을 수 없거나 로드 중 오류가 발생한 경우
            System.err.println("❌ Failed to load game-view.fxml");
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
            
            // 사용자에게 오류 알림 (향후 개선 가능)
            // 예: Alert 다이얼로그 표시
        }
    }

    /**
     * EXIT 버튼 액션 - 애플리케이션 종료
     * 
     * Platform.exit()는 JavaFX 애플리케이션을 정상적으로 종료합니다.
     * 이것은 단순히 System.exit()를 호출하는 것보다 좋습니다.
     * 왜냐하면 JavaFX가 정리 작업을 수행할 수 있기 때문입니다.
     * 
     * TetrisApplication의 stop() 메서드가 자동으로 호출되어
     * Spring Context도 깨끗하게 종료됩니다.
     */

    public void handleEndButtonAction() {
        System.out.println("❌ Exit button clicked - Closing application");
        System.out.println("👋 Goodbye!");
        Platform.exit();
    }
}
