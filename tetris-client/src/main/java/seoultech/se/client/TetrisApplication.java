package seoultech.se.client;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import seoultech.se.client.service.SettingsService;

/**
 * 🎮 JavaFX + Spring Boot 통합 애플리케이션
 *
 * JavaFX를 메인으로 하고 Spring Boot를 DI 컨테이너로 사용하는 통합 구조
 * - init()에서 Spring Boot 컨텍스트 초기화
 * - JavaFX UI와 Spring Boot 서비스 레이어 연동
 * - 독립적으로 실행 가능한 데스크톱 애플리케이션
 */
@SpringBootApplication
// 1. 다른 모듈의 Component(@Service, @Controller 등)를 스캔하기 위한 설정
@ComponentScan(basePackages = {"seoultech.se.client", "seoultech.se.backend"})
// 2. 다른 모듈의 JPA Repository를 스캔하기 위한 설정
@EnableJpaRepositories(basePackages = "seoultech.se.backend")
// 3. 다른 모듈의 JPA Entity(@Entity)를 스캔하기 위한 설정
@EntityScan(basePackages = "seoultech.se.backend")
public class TetrisApplication extends Application {

    private ConfigurableApplicationContext springContext;

    /**
     * 🚀 JavaFX 초기화 단계에서 Spring Boot 컨텍스트 시작
     */
    @Override
    public void init() {
        // JavaFX와 Spring Boot 통합 초기화
        System.setProperty("java.awt.headless", "false");
        System.setProperty("spring.main.web-application-type", "none");

        springContext = SpringApplication.run(TetrisApplication.class);
        System.out.println("✅ Spring Boot context initialized with JavaFX");
    }

    /**
     * 🎨 JavaFX UI 시작
     */
    @Override
    public void start(Stage primaryStage) throws IOException{
        SettingsService settingsService = springContext.getBean(SettingsService.class);
        settingsService.setPrimaryStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(TetrisApplication.class.getResource("/view/main-view.fxml"));
        loader.setControllerFactory(springContext::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root, settingsService.stageWidthProperty().get(), settingsService.stageHeightProperty().get());
        
        primaryStage.setTitle("Tetris Project");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);  // 창 크기 조절 불가
        primaryStage.show();

        System.out.println("✅ JavaFX UI started with main-view.fxml");
    }

    /**
     * 🛑 애플리케이션 종료 시 Spring 컨텍스트 정리
     */
    @Override
    public void stop() {
        if (springContext != null) {
            springContext.close();
            System.out.println("✅ Spring Boot context closed");
        }
        Platform.exit();
    }

    /**
     * 🎯 메인 애플리케이션 진입점
     */
    public static void main(String[] args) {
        launch(args);
    }
}
