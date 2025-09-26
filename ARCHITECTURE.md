# 🏗️ 프로젝트 아키텍처

> Java 21 LTS + Spring Boot 3.5.6 + JavaFX 21 멀티모듈 통합 아키텍처

## 📋 목차
- [🏗️ 모듈 구조](#️-모듈-구조)
- [🔗 의존성 관계](#-의존성-관계)
- [🚀 실행 방식](#-실행-방식)
- [📦 빌드 시스템](#-빌드-시스템)

---

## 🏗️ 모듈 구조

```
tetris-app/
├── tetris-core/      # 🎯 핵심 비즈니스 로직 (순수 Java)
├── tetris-backend/   # ⚙️ Spring Boot 웹 서비스 레이어  
├── tetris-client/    # 🖥️ JavaFX + Spring Boot 메인 애플리케이션
└── tetris-swing/     # 📱 레거시 Swing GUI (독립)
```

### 🎯 tetris-core
- **역할**: 핵심 도메인 로직, 공통 유틸리티
- **의존성**: 최소한 (lombok, testing만)
- **특징**: 외부 프레임워크 없는 순수 Java

### ⚙️ tetris-backend  
- **역할**: REST API, 비즈니스 서비스, 데이터 액세스
- **의존성**: Spring Boot, JPA, H2 Database
- **실행**: 독립 웹 서버 또는 라이브러리로 사용

### 🖥️ tetris-client
- **역할**: JavaFX GUI + Spring Boot DI 통합
- **의존성**: JavaFX, Spring Boot (웹 서버 제외)
- **특징**: Spring Boot를 DI 컨테이너로만 사용

---

## 🔗 의존성 관계

```
tetris-client
    ├─── tetris-backend (Spring 서비스 사용)
    └─── tetris-core (직접 사용)
         
tetris-backend  
    └─── tetris-core (도메인 로직 사용)
```

### Version Catalog 구조
```toml
# gradle/libs.versions.toml
[libraries]
# 공통 의존성
common-lombok = { ... }
common-junit-jupiter = { ... }

# 백엔드 전용  
backend-spring-boot-starter = { ... }
backend-h2-database = { ... }

# 클라이언트 전용
client-javafx-controls = { ... }
client-spring-boot-starter = { ... }
```

---

## 🚀 실행 방식

### 1️⃣ 통합 실행 (권장)
```bash
./gradlew :tetris-client:run
```
- JavaFX GUI 애플리케이션 시작
- Spring Boot DI 컨테이너 초기화 (웹 서버 없음)
- 모든 백엔드 서비스를 GUI에서 직접 사용

### 2️⃣ 백엔드 독립 실행
```bash
./gradlew :tetris-backend:bootRun
```
- Spring Boot 웹 서버 시작 (포트: 8080)
- REST API 엔드포인트 제공
- 개발/테스트용 독립 서비스

### 3️⃣ 스크립트 실행
```bash
# 빌드 + 실행
./script_files/build-and-run.sh

# 클라이언트만 실행  
./script_files/run-tetris.sh

# 백엔드만 실행
./script_files/run-backend.sh

# 통합 개발 도구
./tetris.sh [build|client|backend|test|env]
```

---

## 📦 빌드 시스템

### Gradle 멀티모듈
```kotlin
// settings.gradle.kts
include("tetris-core")
include("tetris-backend") 
include("tetris-client")
```

### 빌드 순서
```
1. tetris-core:jar
2. tetris-backend:jar (depends on core)
3. tetris-client:bootJar (depends on backend + core)
```

### 주요 Task들
```bash
./gradlew clean build          # 전체 빌드
./gradlew :tetris-client:bootJar  # 실행 가능 JAR 생성
./gradlew test                 # 모든 모듈 테스트
./gradlew :tetris-backend:bootRun # 백엔드 서버 실행
./gradlew :tetris-client:run      # 클라이언트 실행
```

---

## 🔧 개발 환경

### 필수 요구사항
- **Java 21 LTS** (Amazon Corretto 권장)
- **Gradle 8.12+** (Wrapper 사용)

### IDE 설정
- **VS Code**: Java Extension Pack + Lombok 지원
- **IntelliJ**: Java 21 + Gradle + Spring Boot 플러그인

### 환경 확인
```bash
./tetris.sh env    # 개발 환경 체크
```

---

## 📚 참고 문서

- **[DEVELOPMENT.md](../DEVELOPMENT.md)**: 상세한 개발 가이드
- **[README.md](../README.md)**: 프로젝트 개요 및 빠른 시작
- **[gradle/libs.versions.toml](../gradle/libs.versions.toml)**: Version Catalog 설정
