/*
 * Tetris Application - Multi-module Project Settings
 * Java 21 LTS + Spring Boot + JavaFX 통합 프로젝트
 */
plugins {
    // JDK를 자동으로 다운로드해주는 플러그인
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// 루트 프로젝트 이름
rootProject.name = "tetris-app"

// 3개의 핵심 모듈 포함
include("tetris-core")     // 🎯 핵심 데이터 및 알고리즘
include("tetris-backend")  // ⚙️ Spring Boot 서비스 레이어  
include("tetris-client")   // 🖥️ JavaFX + Spring Boot 메인 애플리케이션
