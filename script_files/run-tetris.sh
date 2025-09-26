#!/bin/bash

# 🎮 Tetris Desktop Application 실행 스크립트
# Java 21 LTS + JavaFX + Spring Boot (JAR 직접 실행)

echo "🎮 Tetris Desktop Application 시작..."
echo "========================================="

# JAR 파일 경로
JAR_FILE="tetris-client/build/libs/tetris-desktop-app-java21-1.0.0-SNAPSHOT.jar"

# JAR 파일 존재 확인
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR 파일을 찾을 수 없습니다: $JAR_FILE"
    echo "🔧 빌드를 먼저 실행하세요: ./gradlew :tetris-client:bootJar"
    exit 1
fi

echo "✅ JAR 파일 발견: $JAR_FILE"
echo "🚀 애플리케이션 시작 중..."
echo ""

# JavaFX + Spring Boot 애플리케이션 실행 (최적화된 JVM args)
java \
    --add-opens javafx.graphics/com.sun.javafx.application=ALL-UNNAMED \
    --add-opens javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED \
    --add-opens java.base/java.lang=ALL-UNNAMED \
    --add-opens java.base/java.util.concurrent=ALL-UNNAMED \
    -jar "$JAR_FILE"

echo ""
echo "========================================="
echo "🎮 Tetris 애플리케이션이 종료되었습니다."
