package seoultech.se.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * 🎮 JavaFX + Spring Boot 통합 애플리케이션
 * 
 * JavaFX를 메인으로 하고 Spring Boot를 DI 컨테이너로 사용하는 통합 구조
 * - init()에서 Spring Boot 컨텍스트 초기화
 * - JavaFX UI와 Spring Boot 서비스 레이어 연동
 * - 독립적으로 실행 가능한 데스크톱 애플리케이션
 */
@SpringBootApplication(scanBasePackages = {"seoultech.se.backend", "seoultech.se.client"})
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
    public void start(Stage primaryStage) {
        // 기본 UI 구성 (팀에서 자유롭게 수정 가능)
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 20; -fx-alignment: center;");
        root.getChildren().addAll(
            new Label("🚀 Spring Boot + JavaFX 통합 완료"),
            new Label("팀에서 자유롭게 UI를 구현해주세요"),
            new Label("Spring DI 컨테이너 사용 가능")
        );
        
        Scene scene = new Scene(root, 500, 350);
        primaryStage.setTitle("Tetris Project - Integrated Architecture");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("✅ JavaFX UI started with Spring integration");
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
