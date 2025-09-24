/*
 * Tetris Backend Module  
 * ⚙️ Spring Boot 기반 서비스 레이어 (웹 기능 제외)
 * - 게임 서비스 (점수, 레벨, 설정 등)
 * - 데이터 액세스 레이어 (H2 인메모리 DB)
 * - 비즈니스 로직 처리
 * - Desktop 전용 (웹 기능 없음)
 */
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    `java-library`  // 라이브러리로도 사용됨
}

description = "Tetris Backend Services (Desktop Only)"

dependencies {
    // 🎯 Core 모듈 의존성
    api(project(":tetris-core"))
    
    // 🌱 Spring Boot 핵심 (웹 기능 제외)
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    // 🗄️ 데이터베이스 (H2 - 데스크톱 앱용) - 필요시에만 활성화
    // implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // runtimeOnly("com.h2database:h2")
    
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

// 📦 JAR 설정 - 실행 가능하지 않은 라이브러리 JAR
tasks.jar {
    archiveBaseName.set("tetris-backend")
    enabled = true  // plain JAR 생성 활성화
}

// ⚠️ bootJar는 비활성화 (라이브러리로 사용하므로)
tasks.bootJar {
    enabled = false
}

// 🧪 테스트 설정
tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
