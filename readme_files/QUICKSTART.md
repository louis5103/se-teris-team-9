# 🚀 빠른 실행 가이드

## ⚡ 즉시 실행

### 통합 실행 (추천)
```bash
cd tetris-client && ../gradlew run
```
- **결과**: JavaFX GUI + Spring Boot 통합 시스템

### 백엔드 독립 실행  
```bash
cd tetris-backend && ../gradlew bootRun
```
- **결과**: REST API 서버 (http://localhost:8080)

### API 테스트
```bash
curl http://localhost:8080/api/status
```

## 📁 주요 파일들

### 통합 아키텍처
- `tetris-client/src/main/java/seoultech/se/client/TetrisApplication.java`
  - JavaFX + Spring Boot 통합 진입점
  - `@SpringBootApplication` + JavaFX `Application`
  
### 백엔드 서비스  
- `tetris-backend/src/main/java/seoultech/se/backend/TetrisBackendApplication.java`
  - Spring Boot 웹 서버 진입점
- `tetris-backend/src/main/java/seoultech/se/backend/controller/GameController.java`
  - REST API 엔드포인트 (`/api/status`)
- `tetris-backend/src/main/java/seoultech/se/backend/service/GameService.java`
  - 비즈니스 로직 서비스

## 🛠️ 개발 모드

### 통합 개발 (핫 리로드)
```bash
cd tetris-client && ../gradlew run --continuous
```

### 백엔드 개발 (자동 재시작)
```bash  
cd tetris-backend && ../gradlew bootRun --continuous
```

## 📚 문서

- **[DEVELOPMENT.md](DEVELOPMENT.md)** - 상세 개발 가이드
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - 아키텍처 상세
- **[README.md](../README.md)** - 프로젝트 개요

## 🎯 핵심 특징

✅ **Spring Boot + JavaFX 통합** - 하나의 애플리케이션에서 두 프레임워크 동시 활용  
✅ **모듈별 독립 개발** - 백엔드와 프론트엔드 분리 개발 가능  
✅ **의존성 주입** - JavaFX 컨트롤러에서 Spring `@Autowired` 사용  
✅ **Gradle 멀티모듈** - 통합 빌드 시스템  

---

**💡 Tip**: 개발 시작 전에 통합 실행(`tetris-client:run`)으로 전체 시스템이 정상 동작하는지 확인하세요!
