/*
 * 'tetris-client' 모듈의 빌드 설정입니다.
 * JavaFX UI와 애플리케이션의 최종 실행을 담당합니다.
 */
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    // JavaFX GUI를 위한 플러그인을 추가합니다.
    id("org.openjfx.javafxplugin") version "0.1.0"
    // 이 모듈이 실행 가능한 애플리케이션임을 명시합니다.
    application
}

// JavaFX 설정을 정의합니다.
javafx {
    version = "17" // Java 11과 호환되는 안정적인 버전
    modules = listOf("javafx.controls", "javafx.fxml")
}

// 실행 가능한 애플리케이션의 메인 클래스를 지정합니다.
application {
    mainClass.set("seoultech.se.client.TetrisClientApplication")
}

dependencies {
    // ✅ 핵심: 'tetris-backend' 모듈을 의존성으로 추가하여 서비스 로직을 사용합니다.
    implementation(project(":tetris-backend"))

    // Spring Boot의 핵심 기능을 사용하기 위한 의존성을 추가합니다.
    implementation("org.springframework.boot:spring-boot-starter")
}
