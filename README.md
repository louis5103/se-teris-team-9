# 🎮 Tetris Desktop Application (Module Integration Framework)
> Java 21 LTS + Spring Boot + JavaFX Multi-module Project

## 📋 목차

- [🎯 프로젝트 개요](#-프로젝트-개요)
- [🏗️ 모듈 구조](#️-모듈-구조)  
- [🚀 실행 방법](#-실행-방법)
- [📝 개발 가이드](#-개발-가이드)
- [🌿 브랜치 네이밍 규칙](#-브랜치-네이밍-규칙)

## � 프로젝트 개요

**Spring Boot + JavaFX 통합 아키텍처**를 기반으로 한 멀티모듈 개발 프레임워크입니다.

> 🚀 **통합 실행 방법**: `cd tetris-client && ../gradlew run`
> 
> 📚 **상세한 개발 가이드**: [DEVELOPMENT.md](./DEVELOPMENT.md)
> 
> 🏗️ **아키텍처 상세**: [ARCHITECTURE.md](readme_files/ARCHITECTURE.md)

## 🏗️ 모듈 구조

```
tetris-app/
├── tetris-core/          # 핵심 비즈니스 로직 (공통 라이브러리)
├── tetris-backend/       # Spring Boot 웹 서버
├── tetris-client/        # JavaFX 데스크톱 클라이언트  
├── tetris-swing/         # Swing GUI (옵션)
└── build.gradle.kts      # 루트 프로젝트 설정
```

### 📦 각 모듈별 역할

- **tetris-core** - 공통 비즈니스 로직 (순수 Java 라이브러리)
  - 도메인 모델 및 핵심 로직 구현
  - 다른 모듈에서 공통으로 사용하는 유틸리티
- **tetris-backend** - Spring Boot 웹 서버
  - REST API 제공 (`@RestController`)
  - 비즈니스 서비스 레이어 (`@Service`) 
  - 데이터 접근 및 영속성 관리
- **tetris-client** - JavaFX 데스크톱 클라이언트
  - **통합 실행 진입점** (`@SpringBootApplication`)
  - JavaFX GUI 컨트롤러 (`@Component`)
  - Spring Boot + JavaFX 통합 아키텍처

### 🔄 통합 아키텍처 동작 방식

1. **JavaFX Application 시작** → `TetrisApplication.java`
2. **Spring Boot Context 초기화** → `init()` 메서드에서 DI 컨테이너 생성
3. **컴포넌트 스캔** → 백엔드와 클라이언트 패키지 전체 스캔
4. **의존성 주입** → JavaFX 컨트롤러에서 Spring 서비스 사용

```java
// 통합 실행 예시: JavaFX에서 Spring 서비스 사용
@Component
public class MainController {
    @Autowired
    private GameService gameService;  // 백엔드 서비스 자동 주입
    
    @FXML
    private void handleAction() {
        String status = gameService.getStatus();  // Spring DI 활용
        // JavaFX UI 업데이트
    }
}
```
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

### ⭐ 1. 통합 실행 (추천)

**Spring Boot + JavaFX 통합 실행:**
```bash
cd tetris-client
../gradlew run
```
- ✅ JavaFX GUI 애플리케이션 시작
- ✅ Spring Boot DI 컨테이너 자동 초기화  
- ✅ 모든 모듈이 통합된 환경에서 실행

### 🌐 2. 백엔드 독립 실행

**Spring Boot 웹 서버 실행:**
```bash
cd tetris-backend  
../gradlew bootRun
```
- ✅ REST API 서버 시작 (http://localhost:8080)
- ✅ H2 데이터베이스 콘솔 활성화
- ✅ 백엔드 개발/테스트 환경

**API 테스트:**
```bash
curl http://localhost:8080/api/status
```

### 🖥️ 3. 전체 빌드

```bash
# 루트에서 모든 모듈 빌드
./gradlew build

# 실행 가능한 JAR 생성
./gradlew bootJar
```

### 🛠️ 개발 모드 실행

```bash
# 통합 개발 (핫 리로드)
cd tetris-client
../gradlew run --continuous

# 백엔드 개발 (자동 재시작)  
cd tetris-backend
../gradlew bootRun --continuous
```

## 📝 개발 가이드

> 🚀 **상세한 개발 가이드는 [DEVELOPMENT.md](readme_files/DEVELOPMENT.md)를 참고하세요!**

### 👥 팀 개발 워크플로우

**백엔드 개발자:**
```bash
cd tetris-backend
../gradlew bootRun  # 독립 실행으로 API 개발
```

**프론트엔드 개발자:**  
```bash
cd tetris-client
../gradlew run     # 통합 실행으로 UI 개발
```

**통합 테스트:**
```bash
cd tetris-client
../gradlew run     # 전체 시스템 통합 테스트
```

### 🏗️ 아키텍처 특징

📄 **[상세 아키텍처 가이드](readme_files/ARCHITECTURE.md)**

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
- **Spring Boot 3.3.3** - 서비스 레이어 및 DI
- **JavaFX 21** - 모던 Desktop GUI
- **Gradle 8.5** - 멀티모듈 빌드 시스템
- **H2 Database** - 개발용 인메모리 데이터베이스

## 🌿 브랜치 네이밍 규칙

> 📋 **상세한 브랜치 가이드는 [readme_files/BRANCH_NAMING.md](./readme_files/BRANCH_NAMING.md)를 참고하세요.**

### 기본 규칙
```bash
# 모듈별 기능 개발
feature/[모듈명]/[기능명]

# 예시
feature/backend/user-api      # 백엔드 사용자 API 개발
feature/client/game-ui        # 클라이언트 게임 UI 개발  
feature/core/block-logic      # 코어 블록 로직 개발

# 통합 기능
feature/integration/[기능명]  # 모듈 간 통합 기능
```

## 📞 문의 및 기여

- **이슈 리포팅**: GitHub Issues 활용
- **개발 문의**: 팀 개발자 또는 프로젝트 관리자 연락
- **기여 가이드**: Pull Request 템플릿 준수

---

**🎯 Quick Start:**  
```bash
git clone [repository-url]
cd tetris-app
cd tetris-client && ../gradlew run
```

**💡 개발 팁:** 통합 실행(`tetris-client:run`)으로 전체 시스템을 확인하면서 개발하세요!
Feat/123/Add-User            # 대문자 사용
feat/123/add_user            # 언더스코어 사용
feat/123/add.user            # 점 사용
