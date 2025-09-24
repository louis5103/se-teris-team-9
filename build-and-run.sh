#!/bin/bash

# 🎮 Tetris Desktop Application - 빌드 & 실행 스크립트
# Java 21 LTS + JavaFX + Spring Boot

echo "🎮 Tetris Desktop Application - 빌드 & 실행"
echo "========================================="

# 1. 전체 프로젝트 빌드
echo "📦 프로젝트 빌드 중..."
./gradlew clean build

if [ $? -ne 0 ]; then
    echo "❌ 빌드 실패!"
    exit 1
fi

echo "✅ 빌드 완료!"
echo ""

# 2. 애플리케이션 실행
echo "🚀 애플리케이션 실행 중..."
echo "========================================="

# JAR 파일 경로
JAR_FILE="tetris-client/build/libs/tetris-desktop-app-java21-1.0.0-SNAPSHOT.jar"

# JavaFX + Spring Boot 애플리케이션 실행
java \
    --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
    --add-opens javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED \
    --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
    --add-opens javafx.base/com.sun.javafx.binding=ALL-UNNAMED \
    --add-opens javafx.base/com.sun.javafx.event=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.util=ALL-UNNAMED \
    --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
    -Dprism.order=sw \
    -Dprism.text=t2k \
    -jar "$JAR_FILE"

echo ""
echo "========================================="
echo "🎮 Tetris 애플리케이션이 종료되었습니다."
