/*
 * Tetris Backend Module  
 * ⚙️ Spring Boot 기반 서비스 레이어
 * - 게임 서비스 (점수, 레벨, 설정 등)
 * - 데이터 액세스 레이어 (H2 인메모리 DB)
 * - 비즈니스 로직 처리
 * - 독립 실행 가능 (개발/테스트용)
 * - 라이브러리로도 사용 가능 (client 통합시)
 */
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `java-library`  // 라이브러리로도 사용됨
    application     // 독립 실행도 가능
}

description = "Tetris Backend Services (Standalone + Library)"

// 🚀 독립 실행을 위한 메인 클래스 설정
application {
    mainClass.set("seoultech.se.backend.TetrisBackendApplication")
}

dependencies {
    // 🎯 Core 모듈 의존성
    api(project(":tetris-core"))
    
    // 🌱 Spring Boot 핵심
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // 🌐 개발/테스트용 웹 기능 (독립 실행시에만)
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // 🗄️ 데이터베이스 (H2) - 활성화
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
    
    // ⚙️ 설정 관리
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    // 🔧 개발 도구
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    // 🧪 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
}

// 📦 JAR 설정 - 두 가지 모드 지원
tasks.jar {
    archiveBaseName.set("tetris-backend")
    enabled = true  // plain JAR 생성 활성화 (라이브러리용)
}

// 🚀 bootJar는 독립 실행용으로 활성화
tasks.bootJar {
    archiveBaseName.set("tetris-backend-standalone")
    enabled = true  // 독립 실행 가능한 JAR
    archiveClassifier.set("boot")  // 구분을 위한 classifier
}

// 🧪 테스트 설정
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
