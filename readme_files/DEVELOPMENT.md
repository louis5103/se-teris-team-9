# 🚀 개발자 가이드

## 📋 프로젝트 개요

테트리스 애플리케이션의 모듈별 개발 환경 및 통합 실행 가이드입니다.
Spring Boot + JavaFX 통합 아키텍처를 기반으로 한 멀티모듈 프로젝트입니다.

## 🏗️ 모듈 구조

```
tetris-app/
├── tetris-core/          # 핵심 비즈니스 로직 (공통 모듈)
├── tetris-backend/       # Spring Boot 웹 서버
├── tetris-client/        # JavaFX 데스크톱 클라이언트
├── tetris-swing/         # Swing GUI (옵션)
└── build.gradle.kts      # 루트 프로젝트 설정
```

## 🎯 실행 환경

### 1. 통합 모듈 실행 (추천)

**🌟 Spring Boot + JavaFX 통합 실행**

```bash
# tetris-client에서 통합 실행
cd tetris-client
../gradlew run

# 또는 루트에서 직접 실행
./gradlew :tetris-client:run
```

**실행 결과:**
- ✅ JavaFX GUI 애플리케이션 시작
- ✅ Spring Boot DI 컨테이너 자동 초기화
- ✅ 백엔드 서비스들과 자동 연동
- ✅ 통합 상태 확인 UI 제공

**특징:**
- Spring Boot의 `web-application-type: none` 설정으로 웹 서버 없이 DI만 활용
- JavaFX `Application.init()` 메서드에서 Spring 컨텍스트 초기화
- 모든 `@Service`, `@Component` 빈들이 JavaFX에서 사용 가능

### 2. 백엔드 독립 실행

**🌐 Spring Boot Web Server**

```bash
# tetris-backend에서 독립 실행
cd tetris-backend
../gradlew bootRun

# 또는 루트에서 직접 실행
./gradlew :tetris-backend:bootRun
```

**실행 결과:**
- ✅ Tomcat 서버 시작 (포트 8080)
- ✅ H2 인메모리 데이터베이스 초기화
- ✅ REST API 엔드포인트 활성화
- ✅ DevTools로 개발 모드 최적화

**접근 가능한 엔드포인트:**
- `http://localhost:8080/api/status` - 서비스 상태 확인
- `http://localhost:8080/h2-console` - H2 데이터베이스 콘솔

### 3. 클라이언트 독립 실행

**🖥️ JavaFX GUI Only**

```bash
# tetris-client에서 GUI만 실행 (Spring 통합 없이)
cd tetris-client
../gradlew runJavaFX

# 개발 중인 경우 (핫 리로드)
../gradlew run --continuous
```

## 🛠️ 개발 환경 설정

### 필수 요구사항

- **Java 21+** (OpenJDK 또는 Oracle JDK)
- **Gradle 8.5+** (프로젝트에 포함된 gradlew 사용)
- **IDE**: IntelliJ IDEA, Eclipse, VS Code

### IDE 설정

**IntelliJ IDEA:**
1. "Open" → `build.gradle.kts` 선택
2. "Open as Project" 클릭
3. Gradle 자동 임포트 완료 대기
4. 각 모듈이 별도 모듈로 인식되는지 확인

**VS Code:**
1. Java Extension Pack 설치
2. 프로젝트 루트 폴더 열기
3. `Ctrl+Shift+P` → "Java: Reload Projects" 실행

## 🏃‍♂️ 개발 워크플로우

### 백엔드 개발자

1. **독립 개발 환경 시작**
   ```bash
   cd tetris-backend
   ../gradlew bootRun --continuous
   ```

2. **API 개발**
   - `src/main/java/seoultech/se/backend/controller/` - REST Controller
   - `src/main/java/seoultech/se/backend/service/` - 비즈니스 로직
   - `src/main/resources/application.properties` - 설정 파일

3. **테스트**
   ```bash
   # 단위 테스트
   ../gradlew test
   
   # API 테스트
   curl http://localhost:8080/api/status
   ```

### 프론트엔드 개발자

1. **JavaFX 개발**
   ```bash
   cd tetris-client
   ../gradlew run --continuous
   ```

2. **UI 개발**
   - `src/main/java/seoultech/se/client/controller/` - JavaFX Controller
   - `src/main/resources/` - FXML, CSS 파일

### 풀스택 통합 테스트

1. **통합 환경 실행**
   ```bash
   cd tetris-client
   ../gradlew run
   ```

2. **통합 기능 확인**
   - JavaFX UI가 Spring 서비스와 연동되는지 확인
   - DI가 정상 작동하는지 확인

## 🔧 빌드 및 배포

### 개발용 빌드

```bash
# 전체 프로젝트 빌드
./gradlew build

# 특정 모듈만 빌드
./gradlew :tetris-backend:build
./gradlew :tetris-client:build
```

### 실행 가능한 JAR 생성

```bash
# 백엔드 실행 JAR
./gradlew :tetris-backend:bootJar
# 결과: tetris-backend/build/libs/tetris-backend-boot.jar

# 클라이언트 실행 JAR  
./gradlew :tetris-client:bootJar
# 결과: tetris-client/build/libs/tetris-client-boot.jar
```

### 배포용 실행

```bash
# 백엔드 서버 실행
java -jar tetris-backend/build/libs/tetris-backend-boot.jar

# 통합 클라이언트 실행
java -jar tetris-client/build/libs/tetris-client-boot.jar
```

## 🔍 트러블슈팅

### 자주 발생하는 문제

**1. JavaFX 모듈 오류**
```bash
# 해결: JavaFX 모듈 경로 추가
export JAVAFX_HOME=/path/to/javafx
./gradlew run --args="--module-path $JAVAFX_HOME/lib --add-modules javafx.controls,javafx.fxml"
```

**2. Spring Boot 포트 충돌**
```bash
# 해결: 다른 포트 사용
SERVER_PORT=8081 ./gradlew :tetris-backend:bootRun
```

**3. H2 데이터베이스 접근 불가**
- 브라우저에서 `http://localhost:8080/h2-console` 접근
- JDBC URL: `jdbc:h2:mem:tetris_dev`
- 사용자명: `sa`, 비밀번호: (빈칸)

## 📚 주요 의존성

### 백엔드 (tetris-backend)
- **Spring Boot 3.3.3** - 웹 프레임워크
- **Spring Data JPA** - 데이터 접근
- **H2 Database** - 개발용 인메모리 DB
- **Spring Boot DevTools** - 개발 도구

### 클라이언트 (tetris-client)
- **JavaFX 21** - 데스크톱 GUI
- **Spring Boot 3.3.3** - DI 컨테이너
- **Spring Context** - 의존성 주입

### 공통 (tetris-core)
- **JUnit 5** - 테스트 프레임워크
- **Mockito** - 모킹 프레임워크

## 🤝 팀 협업 가이드

### Git 워크플로우

1. **기능 브랜치 생성**
   ```bash
   git checkout -b feature/[module-name]/[feature-name]
   ```

2. **모듈별 개발**
   - `tetris-backend`: API 및 서비스 로직
   - `tetris-client`: GUI 및 사용자 인터랙션
   - `tetris-core`: 공통 비즈니스 로직

3. **통합 테스트 후 병합**
   ```bash
   # 통합 테스트
   ./gradlew :tetris-client:run
   
   # 문제없으면 PR 생성
   git push origin feature/[module-name]/[feature-name]
   ```

### 코드 스타일

- **Spring Annotations**: `@RestController`, `@Service`, `@Component` 활용
- **Dependency Injection**: `@Autowired` 대신 생성자 주입 권장
- **Logging**: SLF4J 사용
- **Exception Handling**: 전역 예외 처리기 구현

---

**💡 Tip:** 개발 중에는 통합 실행(`tetris-client:run`)을 통해 전체 시스템이 정상 작동하는지 수시로 확인하세요.
