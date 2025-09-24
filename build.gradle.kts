/*
 * Tetris Application - Root Build Configuration
 * Java 21 LTS + Spring Boot 3.3.3 + JavaFX 21 (안정적 조합)
 */
plugins {
    java
    // Spring Boot 3.3.3 (Java 21 LTS 완벽 호환)
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
    // JavaFX 플러그인
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

// 모든 하위 모듈에 공통 적용될 설정
subprojects {
    group = "seoultech.se"
    version = "1.0.0-SNAPSHOT"
    
    apply(plugin = "java")
    
    // ✨ Java 21 LTS 설정 (장기 지원 안정 버전)
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    repositories {
        mavenCentral()
    }
    
    // 🧪 공통 테스트 의존성 (최신 안정 버전)
    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
    
    // ⚙️ 테스트 설정
    tasks.withType<Test> {
        useJUnitPlatform()
        // Java 21 Virtual Threads 및 모듈 시스템 지원
        jvmArgs(
            "--add-opens", "java.base/java.lang=ALL-UNNAMED",
            "--add-opens", "java.base/java.util=ALL-UNNAMED",
            "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED"
        )
    }
    
    // 📦 컴파일 설정  
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
    
    // 🚀 실행 설정
    tasks.withType<JavaExec> {
        // Java 21 최적화 옵션
        jvmArgs("--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED")
    }
}

// 🎯 프로젝트 정보
description = "Tetris Desktop Game - Java 21 LTS Multi-module Application (JavaFX + Spring Boot)"

// ⚡ Gradle 성능 최적화
tasks.wrapper {
    gradleVersion = "8.5"
    distributionType = Wrapper.DistributionType.BIN
}
