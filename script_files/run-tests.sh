#!/bin/bash

# 🧪 Tetris Application 테스트 실행 스크립트
# 모든 모듈의 테스트를 실행합니다

echo "🧪 Tetris Application 테스트 시작..."
echo "========================================="

# 테스트 실행
echo "🔍 모든 모듈의 테스트를 실행합니다..."
./gradlew test

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 모든 테스트가 성공적으로 통과했습니다!"
else
    echo ""
    echo "❌ 일부 테스트가 실패했습니다."
    echo "📋 상세한 결과는 build/reports/tests/ 폴더를 확인하세요."
    exit 1
fi

echo ""
echo "========================================="
echo "🧪 테스트가 완료되었습니다."
