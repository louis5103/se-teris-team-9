# 🎮 Tetris Desktop Application
> Java 21 LTS + Spring Boot + JavaFX Multi-module Project

## 📋 목차

- [🎯 프로젝트 개요](#-프로젝트-개요)
- [🏗️ 아키텍처](#️-아키텍처)  
- [🚀 실행 방법](#-실행-방법)
- [🌿 브랜치 네이밍 규칙](#-브랜치-네이밍-규칙)
- [📝 개발 가이드](#-개발-가이드)

## 🎮 프로젝트 개요

Java 21 LTS + Spring Boot + JavaFX를 활용한 멀티모듈 테트리스 데스크톱 애플리케이션입니다.

> 📚 **상세한 아키텍쳐 가이드는 [ARCHITECTURE.md](./ARCHITECTURE.md)를 참고하세요.**

### 📦 모듈 구조

- **tetris-core** - 핵심 게임 로직 및 도메인 모델 (순수 Java)
  - `TetrisBoard`: 게임 보드 로직 (블록 배치, 라인 제거 등)
  - `TetrisBlockType`: 7가지 테트리스 블록 정의 및 회전
  - `TetrisGameThreadManager`: Java 21 Virtual Threads 활용
- **tetris-backend** - Spring Boot 기반 서비스 레이어
  - `ScoreService`: 점수 및 레벨 관리, 게임 통계
- **tetris-client** - JavaFX GUI 메인 애플리케이션
  - `TetrisApplication`: Spring Boot + JavaFX 통합 진입점
  - `MainController`: 게임 컨트롤러 (키보드 입력, UI 업데이트)

### 🔄 동작 방식

1. **사용자 입력** → JavaFX Controller (`@Component`)
2. **비지니스 로직** → Spring Service (`@Autowired`)
3. **도메인 로직** → Core POJO (직접 호출)
4. **결과 반영** → UI 업데이트

```java
// 예시: JavaFX 컨트롤러에서 Spring 서비스와 Core 모듈 사용
@Component
public class MainController {
    @Autowired
    private ScoreService scoreService;  // Spring 서비스 주입
    
    private void handleKeyPress() {
        // Core 모듈 직접 사용
        TetrisBlockType block = TetrisBlockType.getRandomType();
        
        // Backend 서비스 호출
        scoreService.addScore(4);
    }
}
```

## 🏗️ 아키텍처

📄 **[상세 아키텍처 가이드](./ARCHITECTURE.md)**를 확인하세요!

### 🎆 특징

- **하이브리드 아키텍처**: JavaFX가 메인, Spring Boot가 DI 컨테이너
- **계층형 모듈**: Core → Backend → Client 단방향 의존성
- **프레임워크 격리**: Core 로직은 순수 Java로 구현
- **의존성 주입**: JavaFX 컨트롤러에서 `@Autowired` 사용

```
🎯 tetris-core      # 순수 Java 도메인 로직
    ↓
⚙️ tetris-backend   # Spring Boot 서비스 레이어  
    ↓
🖥️ tetris-client    # JavaFX + Spring Boot 통합
```

### 🛠️ 기술 스택

- **Java 21 LTS** - Virtual Threads 지원
- **Spring Boot 3.3.3** - 서비스 레이어
- **JavaFX 21** - 모던 Desktop GUI
- **Gradle 8.5** - 빌드 도구
- **H2 Database** - 인메모리 데이터베이스 (선택적)

## 🚀 실행 방법

```bash
# 전체 빌드
./gradlew clean build

# 개발 모드 실행 (Gradle로)
./gradlew :tetris-client:bootRun

# JAR 빌드 후 실행
./gradlew :tetris-client:bootJar
java -jar tetris-client/build/libs/tetris-desktop-app-java21-1.0.0-SNAPSHOT.jar

# 간편 실행 스크립트 활용
./run-tetris.sh                    # JAR만 실행
./build-and-run.sh                # 빌드 + 실행

# 모듈별 개별 빌드
./gradlew :tetris-core:build       # Core 모듈만
./gradlew :tetris-backend:build    # Backend 모듈만
./gradlew :tetris-client:build     # Client 모듈만

# 개발 도구
./gradlew :tetris-client:dev        # 개발 모드
./verify-gradle-setup.sh           # 설정 검증
```

## 🌿 브랜치 네이밍 규칙

우리 프로젝트는 일관된 브랜치 네이밍을 위해 자동 검증 시스템을 사용합니다.

### 📋 네이밍 규칙

**패턴:** `타입/이슈번호/설명`

- **타입:** `feat`, `fix`, `docs`, `test`, `chore`, `refactor`, `hotfix`
- **이슈번호:** `123` 또는 `ABC-123` (Jira 스타일)
- **설명:** 소문자, 숫자, 하이픈만 사용

### ✨ 올바른 예시

```
feat/123/add-user-login
fix/456/resolve-login-error
docs/789/update-readme
test/ABC-123/add-unit-tests
hotfix/999/security-patch
refactor/24/modulization-each-domain
```

### 🔧 브랜치 관리 도구

#### 1. 모든 브랜치 검증
```bash
./script_files/validate-all-branches.sh
```

#### 2. 새 브랜치 생성 (자동 검증)
```bash
./script_files/create-branch.sh feat/123/your-feature-name
```

#### 3. 브랜치 이름 변경
```bash
git branch -m old-name new-name
```

### 🚫 잘못된 예시

```
feature-123-add-user          # 잘못된 구분자
Feat/123/Add-User            # 대문자 사용
feat/123/add_user            # 언더스코어 사용
feat/123/add.user            # 점 사용
feat/abc/add-user            # 잘못된 이슈번호
new-feature/123/user         # 허용되지 않는 타입
```

### 🤖 자동 검증

- **GitHub Actions:** Push/PR 시 자동으로 브랜치명 검증
- **Git Hook:** Push 전 로컬에서 검증
- **스크립트:** 브랜치 생성 시 즉시 검증

## 📝 개발 가이드

### Java 21 LTS 환경 설정

```bash
# Homebrew로 Java 21 설치
brew install openjdk@21

# JAVA_HOME 설정
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# 확인
java -version
```

### IDE 설정

- **IntelliJ IDEA:** Java 21, Gradle 8.5
- **VS Code:** Extension Pack for Java 설치