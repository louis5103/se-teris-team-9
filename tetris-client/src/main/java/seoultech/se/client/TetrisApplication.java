package seoultech.se.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * 🎮 Tetris JavaFX + Spring Boot 통합 메인 애플리케이션
 * 
 * 이 클래스는 JavaFX가 메인이고, Spring Boot는 DI 컨테이너 역할을 합니다.
 * - JavaFX: 메인 GUI 애플리케이션
 * - Spring Boot: 서비스 레이어 (로그인, 점수 관리 등)
 * - JavaFX 컨트롤러에서 @Component, @Autowired 등 Spring 어노테이션 사용 가능
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "seoultech.se.client",    // Client 모듈
    "seoultech.se.backend",   // Backend 모듈 
    "seoultech.se.core"       // Core 모듈
})
public class TetrisApplication extends Application {
    
    private static ApplicationContext springContext;

    /**
     * 🚀 메인 진입점 - JavaFX 애플리케이션으로 시작
     */
    public static void main(String[] args) {
        // JavaFX 애플리케이션 시작 (Spring Boot는 init에서 시작)
        Application.launch(TetrisApplication.class, args);
    }

    /**
     * ⚙️ JavaFX 초기화 - Spring Boot 컨테이너를 내장으로 시작
     */
    @Override
    public void init() throws Exception {
        super.init();
        
        // Spring Boot 컨텍스트를 백그라운드에서 시작 (웹 기능 없이)
        springContext = SpringApplication.run(TetrisApplication.class, getParameters().getRaw().toArray(new String[0]));
        
        System.out.println("🌱 Spring Boot DI 컨테이너 시작 완료");
    }

    /**
     * 🎨 JavaFX 애플리케이션 시작
     * Spring Boot 컨텍스트와 연결된 FXML 로더를 사용합니다.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Spring 기반 FXML 로더 생성
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        
        // 🔑 핵심: Spring 컨텍스트를 컨트롤러 팩토리로 설정
        // 이렇게 하면 FXML 컨트롤러에서 @Autowired, @Component 사용 가능
        loader.setControllerFactory(springContext::getBean);
        
        Parent root = loader.load();
        
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/tetris.css").toExternalForm());
        
        primaryStage.setTitle("🎮 Tetris Game - Spring Boot + JavaFX");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
        
        // 창 닫기 이벤트 처리
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            SpringApplication.exit(springContext, () -> 0);
            System.exit(0);
        });
    }

    /**
     * 🛑 애플리케이션 종료시 Spring 컨텍스트도 정리
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        if (springContext != null) {
            SpringApplication.exit(springContext, () -> 0);
        }
    }

    /**
     * 🔧 Spring Context를 반환하는 헬퍼 메서드
     * 다른 JavaFX 클래스에서 Spring Bean에 접근할 때 사용
     */
    public static ApplicationContext getSpringContext() {
        return springContext;
    }
}
