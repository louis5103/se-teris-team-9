#!/bin/bash

# 🎯 Tetris Application - 개발 도구 스크립트
# 프로젝트 빌드, 실행, 테스트를 위한 통합 도구

set -e  # 에러 발생 시 스크립트 중단

# 색깔 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수: 헤더 출력
print_header() {
    echo -e "${BLUE}===============================================${NC}"
    echo -e "${BLUE} 🎯 Tetris Application - $1${NC}"
    echo -e "${BLUE}===============================================${NC}"
}

# 함수: 성공 메시지
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

# 함수: 오류 메시지
print_error() {
    echo -e "${RED}❌ $1${NC}"
}

# 함수: 경고 메시지
print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# 함수: 정보 메시지
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# 함수: 프로젝트 빌드
build_project() {
    print_header "프로젝트 빌드"
    print_info "전체 프로젝트를 빌드합니다..."
    
    ./gradlew clean build
    
    if [ $? -eq 0 ]; then
        print_success "빌드 완료!"
    else
        print_error "빌드 실패!"
        exit 1
    fi
}

# 함수: 백엔드 서버 실행
run_backend() {
    print_header "백엔드 서버 실행"
    print_info "Spring Boot 서버를 시작합니다... (포트: 8080)"
    print_info "종료하려면 Ctrl+C를 누르세요"
    
    ./gradlew :tetris-backend:bootRun
}

# 함수: 클라이언트 실행
run_client() {
    print_header "데스크톱 클라이언트 실행"
    print_info "JavaFX 애플리케이션을 시작합니다..."
    
    ./gradlew :tetris-client:run
}

# 함수: 테스트 실행
run_tests() {
    print_header "테스트 실행"
    print_info "모든 모듈의 테스트를 실행합니다..."
    
    ./gradlew test
    
    if [ $? -eq 0 ]; then
        print_success "모든 테스트 통과!"
    else
        print_error "테스트 실패!"
        exit 1
    fi
}

# 함수: 개발 환경 체크
check_environment() {
    print_header "개발 환경 체크"
    
    # Java 버전 확인
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -eq 21 ]; then
            print_success "Java 21 설치됨"
        else
            print_warning "Java 21이 권장됩니다. 현재 버전: $JAVA_VERSION"
        fi
    else
        print_error "Java가 설치되지 않았습니다!"
    fi
    
    # Gradle 래퍼 확인
    if [ -f "./gradlew" ]; then
        print_success "Gradle Wrapper 확인됨"
    else
        print_error "Gradle Wrapper가 없습니다!"
    fi
}

# 함수: 사용법 출력
show_usage() {
    echo -e "${BLUE}🎯 Tetris Application 개발 도구${NC}"
    echo ""
    echo "사용법: $0 [명령어]"
    echo ""
    echo "명령어:"
    echo "  build      전체 프로젝트 빌드"
    echo "  backend    백엔드 서버 실행"
    echo "  client     데스크톱 클라이언트 실행"
    echo "  test       모든 테스트 실행"
    echo "  check      개발 환경 체크"
    echo "  help       이 도움말 표시"
    echo ""
    echo "예시:"
    echo "  $0 build        # 프로젝트 빌드"
    echo "  $0 client       # 클라이언트 실행"
    echo "  $0 backend      # 백엔드 서버 실행"
}

# 메인 로직
case "$1" in
    "build")
        check_environment
        build_project
        ;;
    "backend")
        run_backend
        ;;
    "client")
        run_client
        ;;
    "test")
        run_tests
        ;;
    "check")
        check_environment
        ;;
    "help"|"--help"|"-h")
        show_usage
        ;;
    "")
        show_usage
        ;;
    *)
        print_error "알 수 없는 명령어: $1"
        echo ""
        show_usage
        exit 1
        ;;
esac
