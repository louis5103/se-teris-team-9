#!/bin/bash

# 🌐 Tetris Backend Server 실행 스크립트
# Spring Boot Web Server (독립 실행)

echo "🌐 Tetris Backend Server 시작..."
echo "========================================="

# 백엔드 서버 실행
echo "🚀 Spring Boot 서버 시작 중... (포트: 8080)"
echo "📋 API 엔드포인트: http://localhost:8080/api/status"
echo "⏹️  종료하려면 Ctrl+C를 누르세요"
echo ""

./gradlew :tetris-backend:bootRun

echo ""
echo "========================================="
echo "🌐 Backend 서버가 종료되었습니다."
