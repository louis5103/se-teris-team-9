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
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
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
    
    // ============================================================================
    // ⚙️ BACKEND MODULE SPECIFIC DEPENDENCIES
    // ============================================================================
    
    // 🌱 Spring Boot Core
    implementation(libs.backend.spring.boot.starter)
    
    // 🌐 Spring Web Bundle (Web + Validation)
    implementation(libs.bundles.backend.spring.web)
    
    // 🗄️ Data Access Bundle (JPA + H2 Database)  
    implementation(libs.backend.spring.boot.starter.data.jpa)
    runtimeOnly(libs.backend.h2.database)
    
    // 🔧 Development Tools Bundle
    implementation(libs.bundles.backend.development)
    annotationProcessor(libs.backend.spring.boot.configuration.processor)
    
    // ============================================================================
    // 🚀 COMMON DEPENDENCIES (모든 모듈 공통)
    // ============================================================================
    
    // 🛠️ Development Tools
    compileOnly(libs.common.lombok)
    annotationProcessor(libs.common.lombok)
    testCompileOnly(libs.common.lombok)
    testAnnotationProcessor(libs.common.lombok)
    
    // 🧪 Testing Dependencies
    testImplementation(libs.backend.spring.boot.starter.test) {
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

// 🧪 테스트 설정 (루트에서 상속받아 일관성 확보)
tasks.test {
    useJUnitPlatform()
    
    // 추가 설정이 필요한 경우에만 여기서 오버라이드
    // 기본 설정은 루트 build.gradle.kts에서 상속됨
}
