#!/bin/bash

# 🎮 Tetris Desktop Application - 빌드 & 실행 스크립트
# Java 21 LTS + JavaFX + Spring Boot (최적화된 설정)

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

# 2. 애플리케이션 실행 (Gradle 방식 - 권장)
echo "🚀 애플리케이션 실행 중..."
echo "========================================="

# Gradle을 통한 실행 (자동으로 최적화된 JVM args 적용)
./gradlew :tetris-client:run

echo ""
echo "========================================="
echo "🎮 Tetris 애플리케이션이 종료되었습니다."
