# 🎮 Tetris Multi-Module Architecture Guide

## 📋 목차
1. [아키텍처 개요](#1-아키텍처-개요)
2. [모듈별 상세 구조](#2-모듈별-상세-구조)
3. [의존성 관계 및 데이터 흐름](#3-의존성-관계-및-데이터-흐름)
4. [Spring Boot + JavaFX 통합 방식](#4-spring-boot--javafx-통합-방식)
5. [Gradle 멀티모듈 빌드 체계](#5-gradle-멀티모듈-빌드-체계)
6. [모듈 통합 및 배포 프로세스](#6-모듈-통합-및-배포-프로세스)
7. [개발 워크플로우](#7-개발-워크플로우)
8. [모듈별 독립 개발 가이드](#8-모듈별-독립-개발-가이드)
9. [패키지 구조 및 네이밍 컨벤션](#9-패키지-구조-및-네이밍-컨벤션)
10. [개발 가이드라인](#10-개발-가이드라인)

---

## 1. 아키텍처 개요

### 🏗️ 전체 구조
```
tetris-app (Java 21 LTS)
├── tetris-core       🎯 순수 Java 도메인 로직
├── tetris-backend    ⚙️ Spring Boot 서비스 레이어
└── tetris-client     🖥️ JavaFX + Spring Boot 메인 애플리케이션
```

### 🎯 설계 철학
- **관심사의 분리**: 각 모듈은 명확한 책임을 가짐
- **의존성 역전**: 상위 모듈이 하위 모듈에 의존하는 구조
- **테스트 가능성**: 순수 Java 로직과 프레임워크 분리
- **재사용성**: Core 모듈은 다른 UI 프레임워크에서도 사용 가능

### 🔄 동작 흐름
1. **사용자 입력** → JavaFX Controller
2. **비즈니스 로직 호출** → Spring Service
3. **도메인 로직 실행** → Core POJO
4. **결과 반환** → Controller → UI 업데이트

---

## 2. 모듈별 상세 구조

### 🎯 tetris-core (도메인 층)

**역할**: 순수 Java 게임 로직 및 도메인 모델

#### 📦 주요 컴포넌트
```java
seoultech.se.core/
├── model/
│   ├── TetrisBoard.java          // 게임 보드 로직
│   └── TetrisBlockType.java      // 블록 종류 및 회전
└── concurrent/
    └── TetrisGameThreadManager.java  // Java 21 Virtual Threads
```

#### 🎮 TetrisBoard 주요 기능
```java
public class TetrisBoard {
    // 📏 보드 상수
    public static final int BOARD_WIDTH = 10;
    public static final int BOARD_HEIGHT = 20;
    
    // 🎯 핵심 메서드
    public boolean canPlaceBlock(int[][] blockShape, int x, int y)
    public boolean placeBlock(int[][] blockShape, int x, int y, int blockValue)
    public int clearCompletedLines()
    public boolean isGameOver()
    public int getDropDistance(int[][] blockShape, int x, int y)
}
```

#### 🧩 TetrisBlockType 특징
```java
public enum TetrisBlockType {
    I, O, T, S, Z, J, L;  // 7가지 표준 테트리스 블록
    
    // 🔄 회전 메서드
    public int[][] getRotatedShape()
    // 🎲 랜덤 생성
    public static TetrisBlockType getRandomType()
}
```

#### 🧵 Java 21 Virtual Threads 활용
```java
public class TetrisGameThreadManager {
    private final ExecutorService virtualThreadExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    public CompletableFuture<Void> startGameLoop(Runnable gameLogic)
    public CompletableFuture<Void> startBlockDropTimer(Runnable dropLogic, Duration interval)
    public void playSoundAsync(String soundName)
}
```

**특징**:
- ✅ **외부 의존성 없음**: 순수 Java, Spring/JavaFX 독립적
- ✅ **불변성 보장**: 도메인 객체의 상태 안전성
- ✅ **테스트 용이**: Mock 없이 직접 테스트 가능
- ✅ **성능 최적화**: Java 21 Virtual Threads 활용

---

### ⚙️ tetris-backend (서비스 층)

**역할**: Spring Boot 기반 비즈니스 로직 및 서비스

#### 📦 주요 컴포넌트
```java
seoultech.se.backend/
└── service/
    └── ScoreService.java  // 점수 및 게임 상태 관리
```

#### 🏆 ScoreService 상세
```java
@Service  // Spring Bean으로 등록
public class ScoreService {
    // 🔢 Thread-Safe 점수 관리
    private final AtomicLong currentScore = new AtomicLong(0);
    private final AtomicLong highScore = new AtomicLong(0);
    private final AtomicInteger currentLevel = new AtomicInteger(1);
    
    // 🎯 핵심 메서드
    public void addScore(int linesCleared)     // 점수 추가
    public void resetGame()                    // 게임 리셋
    public long getDropInterval()              // 레벨별 낙하 속도
    public String getGameStats()               // 게임 통계
}
```

#### 💡 서비스 특징
- **Thread-Safe**: `AtomicInteger`, `AtomicLong` 사용
- **Spring 관리**: `@Service`로 자동 Bean 등록
- **상태 관리**: 게임 점수, 레벨, 통계 중앙 집중 관리
- **비즈니스 로직**: 점수 계산, 레벨업 조건 등

**설계 원칙**:
- ✅ **단일 책임**: 점수/레벨 관리만 담당
- ✅ **의존성 주입**: 다른 서비스와 느슨한 결합
- ✅ **상태 캡슐화**: 게임 상태를 안전하게 관리
- ✅ **확장성**: 새로운 게임 기능 쉽게 추가 가능

---

### 🖥️ tetris-client (프레젠테이션 층)

**역할**: JavaFX GUI + Spring Boot 통합 메인 애플리케이션

#### 📦 주요 컴포넌트
```java
seoultech.se.client/
├── TetrisApplication.java        // Spring Boot + JavaFX 통합
└── controller/
    └── MainController.java       // 메인 게임 컨트롤러
```

#### 🚀 TetrisApplication - 핵심 통합 지점
```java
@SpringBootApplication
@ComponentScan(basePackages = {
    "seoultech.se.client",    // Client 모듈
    "seoultech.se.backend",   // Backend 모듈 
    "seoultech.se.core"       // Core 모듈
})
public class TetrisApplication extends Application {
    private static ApplicationContext springContext;
    
    // 🎬 JavaFX 시작점
    public static void main(String[] args) {
        Application.launch(TetrisApplication.class, args);
    }
    
    // 🌱 Spring Boot 컨텍스트 초기화
    @Override
    public void init() throws Exception {
        springContext = SpringApplication.run(TetrisApplication.class, ...);
    }
    
    // 🎨 JavaFX 화면 구성
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        
        // 🔑 핵심: Spring 컨텍스트를 컨트롤러 팩토리로 설정
        loader.setControllerFactory(springContext::getBean);
        
        Parent root = loader.load();
        // ... 화면 설정
    }
}
```

#### 🎮 MainController - 게임 로직 조합
```java
@Component  // Spring Bean으로 등록
public class MainController implements Initializable {
    
    // 🌱 Spring 서비스 자동 주입
    @Autowired
    private ScoreService scoreService;
    
    // 🎯 Core 도메인 객체 직접 사용
    private TetrisBlockType currentBlockType;
    
    // 🎨 JavaFX UI 컴포넌트
    @FXML private Canvas gameCanvas;
    @FXML private Label scoreLabel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ✨ Core 모듈 직접 사용
        generateNewBlock();
        // ✨ Backend 서비스 사용
        updateUI();
    }
    
    // 🎲 Core 모듈 직접 호출
    private void generateNewBlock() {
        currentBlockType = TetrisBlockType.getRandomType();
    }
    
    // 📊 Backend 서비스 호출
    private void updateUI() {
        scoreLabel.setText("점수: " + scoreService.getCurrentScore());
        levelLabel.setText("레벨: " + scoreService.getCurrentLevel());
    }
}
```

**통합 특징**:
- ✅ **완전한 DI**: JavaFX 컨트롤러에서 `@Autowired` 사용
- ✅ **직접 접근**: Core 모듈의 POJO 직접 사용
- ✅ **이벤트 처리**: JavaFX 이벤트 → Spring 서비스 → Core 로직
- ✅ **생명주기 관리**: Spring Boot가 애플리케이션 생명주기 관리

---

## 3. 의존성 관계 및 데이터 흐름

### 📈 의존성 그래프
```
tetris-client
    ↓ (depends on)
tetris-backend
    ↓ (depends on)  
tetris-core
```

### 🔄 데이터 흐름 상세

#### 1️⃣ 사용자 입력 처리
```
키보드 입력 → JavaFX Event → MainController
                                    ↓
                             @FXML handleKeyPress()
                                    ↓
                     TetrisBlockType.getRotatedShape()  ← Core 직접 호출
                                    ↓
                        scoreService.addScore()        ← Backend 서비스
                                    ↓
                              UI 업데이트
```

#### 2️⃣ 게임 상태 업데이트
```
게임 이벤트 → ScoreService (Backend)
                    ↓
          AtomicLong/AtomicInteger 업데이트
                    ↓
          MainController.updateUI() 호출
                    ↓
          JavaFX Label 텍스트 변경
```

#### 3️⃣ 블록 로직 실행
```
블록 이동 요청 → MainController
                    ↓
          TetrisBoard.canPlaceBlock()    ← Core 로직
                    ↓
          TetrisBoard.placeBlock()       ← Core 로직
                    ↓
          Canvas 다시 그리기             ← JavaFX 렌더링
```

### 🎯 모듈 간 통신 방식

| 통신 방향 | 방식 | 예시 |
|-----------|------|------|
| Client → Backend | `@Autowired` 의존성 주입 | `scoreService.addScore()` |
| Client → Core | 직접 메서드 호출 | `TetrisBlockType.getRandomType()` |
| Backend → Core | 직접 메서드 호출 | `TetrisBoard.clearCompletedLines()` |

---

## 4. Spring Boot + JavaFX 통합 방식

### 🔑 핵심 통합 메커니즘

#### 1️⃣ Spring Context 초기화
```java
// TetrisApplication.java
@Override
public void init() throws Exception {
    // JavaFX 초기화 전에 Spring 컨텍스트 생성
    springContext = SpringApplication.run(TetrisApplication.class, args);
}
```

#### 2️⃣ Controller Factory 설정
```java
// Spring Bean을 JavaFX 컨트롤러로 사용
loader.setControllerFactory(springContext::getBean);
```

#### 3️⃣ Component Scan 설정
```java
@ComponentScan(basePackages = {
    "seoultech.se.client",    // JavaFX 컨트롤러들
    "seoultech.se.backend",   // Spring 서비스들
    "seoultech.se.core"       // 필요시 Core Bean들
})
```

### 🌟 통합의 장점

#### ✅ **완전한 의존성 주입**
```java
@Component
public class MainController {
    @Autowired
    private ScoreService scoreService;  // Spring 서비스 자동 주입
    
    @Autowired  
    private SoundService soundService;  // 다른 서비스도 주입 가능
}
```

#### ✅ **Spring Boot 생태계 활용**
- `@Service`, `@Repository`, `@Configuration` 모두 사용 가능
- Spring Boot의 자동 설정 및 스타터 활용
- AOP, 트랜잭션, 캐싱 등 Spring 기능 사용

#### ✅ **테스트 용이성**
```java
@SpringBootTest
class MainControllerTest {
    @MockBean
    private ScoreService scoreService;  // Mock 서비스 주입
    
    @Test
    void testGameLogic() {
        // Spring 테스트 컨텍스트 활용
    }
}
```

### 🚀 실행 순서
1. **JavaFX Application.launch()** 호출
2. **init()** 메서드에서 **Spring Boot 컨텍스트** 생성
3. **start()** 메서드에서 **FXML 로딩** 및 **Controller Factory** 설정
4. **JavaFX 컨트롤러**를 **Spring Bean**으로 생성
5. **@Autowired**로 **서비스 의존성** 주입 완료

---

## 5. Gradle 멀티모듈 빌드 체계

### 🏠 프로젝트 구조 및 빌드 설정

#### 📁 물리적 디렉토리 구조
```
tetris-app/
├── settings.gradle.kts          # 프로젝트 및 모듈 정의
├── build.gradle.kts             # 루트 레벨 빌드 설정
├── gradle.properties            # 글로벌 빌드 설정
│
├── tetris-core/                 # 도메인 모듈
│   ├── build.gradle.kts         # Core 빌드 설정
│   └── src/main/java/           # Core 소스 코드
│
├── tetris-backend/              # 서비스 모듈
│   ├── build.gradle.kts         # Backend 빌드 설정
│   └── src/main/java/           # Backend 소스 코드
│
└── tetris-client/               # 애플리케이션 모듈
    ├── build.gradle.kts         # Client 빌드 설정
    └── src/main/java/           # Client 소스 코드
```

#### 📜 빌드 스크립트 역할 분담

**1. settings.gradle.kts** - 모듈 등록 및 기본 설정
```kotlin
rootProject.name = "tetris-app"

// 3개의 핵심 모듈 포함
include("tetris-core")     // 함수형 도메인 로직
include("tetris-backend")  // Spring Boot 서비스 레이어
include("tetris-client")   // JavaFX + Spring 메인 애플리케이션
```

**2. 루트 build.gradle.kts** - 공통 설정 및 플러그인
```kotlin
// 모든 하위 모듈에 공통 적용될 설정
subprojects {
    group = "seoultech.se"
    version = "1.0.0-SNAPSHOT"
    
    apply(plugin = "java")
    
    // Java 21 LTS 설정
    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    repositories {
        mavenCentral()
    }
    
    // 공통 테스트 의존성
    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }
}
```

### 🔄 빌드 프로세스 상세

#### 1️⃣ 의존성 해결 순서
```
1. tetris-core 모듈 컴파일
   └─ JAR 생성: tetris-core-1.0.0-SNAPSHOT.jar
   
2. tetris-backend 모듈 컴파일  
   ├─ Core 모듈 의존성 포함
   └─ JAR 생성: tetris-backend-1.0.0-SNAPSHOT.jar
   
3. tetris-client 모듈 컴파일
   ├─ Backend + Core 모듈 의존성 포함
   └─ 실행 가능한 Spring Boot JAR 생성
```

#### 2️⃣ Gradle 빌드 명령어 체계
```bash
# 전체 프로젝트 빌드 (의존성 순서대로 빌드)
./gradlew build
┌────────────────────────────────────────────────────┐
│ 1. :tetris-core:compileJava           │ Core 컴파일     │
│ 2. :tetris-core:test                  │ Core 테스트       │
│ 3. :tetris-core:jar                   │ Core JAR 생성   │
│                                       │                 │
│ 4. :tetris-backend:compileJava        │ Backend 컴파일   │
│ 5. :tetris-backend:test               │ Backend 테스트     │  
│ 6. :tetris-backend:jar                │ Backend JAR 생성 │
│                                       │                 │
│ 7. :tetris-client:compileJava         │ Client 컴파일    │
│ 8. :tetris-client:test                │ Client 테스트      │
│ 9. :tetris-client:bootJar             │ 실행 가능 JAR   │
└────────────────────────────────────────────────────┘

# 모듈별 개별 빌드
./gradlew :tetris-core:build         # Core만 빌드
./gradlew :tetris-backend:build      # Backend만 빌드 (자동으로 Core 포함)
./gradlew :tetris-client:build       # Client만 빌드 (전체 의존성 포함)

# 선택적 빌드
./gradlew :tetris-client:bootJar     # 실행 가능한 JAR만 생성
./gradlew :tetris-core:jar           # Core 라이브러리 JAR만 생성
```

#### 3️⃣ 모듈 간 의존성 설정

**tetris-backend/build.gradle.kts**
```kotlin
dependencies {
    // 🎯 Core 모듈 의존성
    api(project(":tetris-core"))  // api(): 전이적 의존성
    
    // Spring Boot 스터터
    implementation("org.springframework.boot:spring-boot-starter")
}
```

**tetris-client/build.gradle.kts**
```kotlin
dependencies {
    // 모든 내부 모듈 의존성
    implementation(project(":tetris-core"))     // Core 직접 사용
    implementation(project(":tetris-backend"))  // Backend 서비스 사용
    
    // JavaFX + Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.openjfx:javafx-controls:21")
}
```

### 📋 Gradle Task 의존성 그래프
```
tetris-client:bootJar
    │
    ├─── tetris-client:compileJava
    │       │
    │       ├─── tetris-backend:jar
    │       │       │
    │       │       ├─── tetris-backend:compileJava
    │       │       │       │
    │       │       │       └─── tetris-core:jar
    │       │       │               │
    │       │       │               └─── tetris-core:compileJava
    │       │       │
    │       │       └─── tetris-core:jar (전이적 의존성)
    │       │
    │       └─── tetris-core:jar (직접 의존성)
    │
    └─── processResources (자원 파일 처리)
```

---

## 6. 모듈 통합 및 배포 프로세스

### 📦 Spring Boot JAR 구조 분석

#### 최종 실행 가능한 JAR 내부 구조
```
tetris-desktop-app-java21-1.0.0-SNAPSHOT.jar
├── META-INF/
│   └── MANIFEST.MF                    # 실행 정보
│       Main-Class: org.springframework.boot.loader.launch.JarLauncher
│       Start-Class: seoultech.se.client.TetrisApplication
│
├── BOOT-INF/
│   ├── classes/                       # 메인 애플리케이션 클래스
│   │   └── seoultech/se/client/
│   │       ├── TetrisApplication.class
│   │       └── controller/MainController.class
│   │
│   └── lib/                           # 의존성 JAR 파일들
│       ├── tetris-core-1.0.0-SNAPSHOT.jar         # 도메인 로직
│       ├── tetris-backend-1.0.0-SNAPSHOT.jar       # 서비스 레이어
│       ├── spring-boot-starter-3.3.3.jar           # Spring Boot
│       ├── javafx-controls-21.jar                   # JavaFX 컨트롤
│       ├── javafx-fxml-21.jar                       # JavaFX FXML
│       └── ...(기타 의존성들)
│
└── org/springframework/boot/loader/         # Spring Boot 로더
    └── launch/JarLauncher.class
```

#### 클래스패스 구성 과정
```java
// 1. JarLauncher가 실행되며 클래스패스 구성
ClassPath = [
    "BOOT-INF/classes/",                    // 메인 애플리케이션
    "BOOT-INF/lib/tetris-core-*.jar",       // Core 모듈
    "BOOT-INF/lib/tetris-backend-*.jar",    // Backend 모듈
    "BOOT-INF/lib/spring-boot-*.jar",       // Spring Boot
    "BOOT-INF/lib/javafx-*.jar",            // JavaFX
    "BOOT-INF/lib/*"                        // 모든 의존성
]

// 2. 클래스 로딩 순서
1. seoultech.se.client.TetrisApplication    # 메인 클래스
2. seoultech.se.backend.service.*           # Spring 서비스들  
3. seoultech.se.core.model.*                # 도메인 모델들
4. org.springframework.*                    # Spring 프레임워크
5. javafx.*                                 # JavaFX 라이브러리
```

### 🔄 런타임 실행 프로세스

#### 1️⃣ 실행 명령어 분석
```bash
java -jar tetris-desktop-app-java21-1.0.0-SNAPSHOT.jar
│
├── 1. JVM 시작 및 MANIFEST.MF 읽기
│   └── Main-Class: org.springframework.boot.loader.launch.JarLauncher
│
├── 2. Spring Boot Loader 시작
│   ├── BOOT-INF/lib/*.jar 클래스패스에 추가
│   └── Start-Class 실행: seoultech.se.client.TetrisApplication
│
├── 3. JavaFX Application.launch() 호출
│   └── JavaFX 스레드에서 TetrisApplication.main() 실행
│
├── 4. TetrisApplication.init() 실행
│   └── SpringApplication.run() 통해 Spring 컨텍스트 생성
│       ├── @ComponentScan으로 모든 모듈 스캔
│       ├── ScoreService @Service 등록
│       └── MainController @Component 등록
│
├── 5. TetrisApplication.start() 실행
│   ├── FXML 로딩 및 Spring Bean을 컨트롤러로 사용
│   └── @Autowired로 ScoreService 주입 완료
│
└── 6. JavaFX GUI 화면 표시 및 사용자 입력 대기
```

#### 2️⃣ 모듈 간 통신 흐름
```java
// 사용자가 스페이스바를 누른 경우

1. JavaFX 이벤트 발생
   → MainController.handleKeyPress(KeyEvent)
   
2. Client 모듈에서 Core 모듈 직접 호출
   → TetrisBlockType.getRandomType()         // Core JAR에서 로드
   → tetrisBoard.canPlaceBlock(...)          // Core JAR에서 로드
   
3. Client 모듈에서 Backend 모듈 서비스 호출
   → scoreService.addScore(4)               // Backend JAR에서 로드
   
4. Backend 모듈에서 Core 모듈 호출
   → scoreService 내부에서 TetrisBoard 사용    // Core JAR에서 로드
   
5. 결과를 Client로 반환 및 UI 업데이트
   → JavaFX Canvas에 그리기
```

### 📊 빌드 최적화 전략

#### 점진적 빌드 (Incremental Build)
```bash
# 특정 모듈만 수정된 경우
수정: tetris-core/TetrisBoard.java
└── 영향 범위: tetris-core → tetris-backend → tetris-client

./gradlew build
│
├── :tetris-core:compileJava       # 수정된 파일만 재컴파일
├── :tetris-backend:compileJava    # Core 의존성 변경으로 재컴파일
└── :tetris-client:compileJava     # Backend 의존성 변경으로 재컴파일

# 모듈 독립 빌드로 시간 단축
./gradlew :tetris-core:build      # Core만 빌드 (5초)
vs
./gradlew build                   # 전체 빌드 (15초)
```

#### 빌드 캐시 활용
```bash
# gradle.properties 설정
org.gradle.caching=true
org.gradle.parallel=true

# 결과: 빌드 시간 단축
첫 번째 빌드: 30초
두 번째 빌드: 10초 (캐시 활용)
일부 수정 후: 5초 (점진적 빌드)
```

---

## 7. 개발 워크플로우

### 📝 전체 개발 프로세스

#### 1️⃣ 프로젝트 시작 워크플로우
```bash
# 1. 프로젝트 초기 설정
git clone <repository>
cd tetris-app

# 2. 개발 환경 확인
java -version                    # Java 21 LTS 확인
./gradlew --version             # Gradle 8.5 확인

# 3. 전체 빌드 및 테스트
./gradlew clean build           # 전체 빌드
./verify-gradle-setup.sh        # 설정 검증

# 4. 애플리케이션 실행 테스트
./gradlew :tetris-client:bootRun
# 또는
./run-tetris.sh
```

#### 2️⃣ 일상 개발 워크플로우
```bash
# 아침: 최신 코드 동기화
git pull origin main
./gradlew clean build           # 전체 리빌드

# 기능 개발: 단일 모듈 집중 개발
./gradlew :tetris-core:build    # Core 모듈만 수정 시
./gradlew :tetris-core:test     # Core 모듈 테스트

# 통합 테스트: 전체 애플리케이션 테스트
./gradlew :tetris-client:bootRun

# 커밋 전: 전체 테스트
./gradlew test                  # 모든 모듈 테스트
./gradlew build                 # 전체 빌드 확인
```

#### 3️⃣ 피처 개발 워크플로우
```bash
# 새 기능 브랜치 생성
./script_files/create-branch.sh feat/25/add-sound-effects

# 단계별 개발 및 테스트
# 1단계: Core 모듈에 사운드 모델 추가
cd tetris-core
# ... SoundEffect.java 구현
./gradlew :tetris-core:test

# 2단계: Backend 모듈에 사운드 서비스 추가
cd ../tetris-backend
# ... SoundService.java 구현
./gradlew :tetris-backend:test

# 3단계: Client 모듈에 UI 통합
cd ../tetris-client
# ... MainController 에 사운드 기능 추가
./gradlew :tetris-client:bootRun    # 통합 테스트

# 4단계: 전체 테스트 및 PR
cd ..
./gradlew clean build
git add .
git commit -m "feat: add sound effects system"
git push origin feat/25/add-sound-effects
```

### 🔄 통합 개발 패턴

#### 모듈 간 인터페이스 설계 워크플로우
```java
// 1단계: Core 모듈에서 인터페이스 정의
// tetris-core/src/main/java/seoultech/se/core/model/SoundEffect.java
public enum SoundEffect {
    LINE_CLEAR, BLOCK_DROP, GAME_OVER;
    
    public String getFileName() {
        return name().toLowerCase() + ".wav";
    }
}

// 2단계: Backend 모듈에서 서비스 구현
// tetris-backend/src/main/java/seoultech/se/backend/service/SoundService.java
@Service
public class SoundService {
    public void playSound(SoundEffect effect) {
        // Core 모듈의 enum 사용
        String fileName = effect.getFileName();
        // 사운드 재생 로직...
    }
}

// 3단계: Client 모듈에서 통합 사용
// tetris-client/src/main/java/seoultech/se/client/controller/MainController.java
@Component
public class MainController {
    @Autowired
    private SoundService soundService;  // Backend 서비스
    
    private void onLineClear() {
        soundService.playSound(SoundEffect.LINE_CLEAR);  // Core enum 사용
    }
}
```

#### 테스트 주도 개발 (TDD) 워크플로우
```bash
# 1. Core 모듈: 도메인 로직 TDD
cd tetris-core

# 테스트 단계
./gradlew test --tests "*TetrisBoardTest"     # 특정 테스트
./gradlew test --continuous                   # 지속적 테스트

# 2. Backend 모듈: 서비스 테스트
cd ../tetris-backend
./gradlew test                               # Spring Boot Test

# 3. Client 모듈: 통합 테스트
cd ../tetris-client  
./gradlew test                               # JavaFX + Spring 테스트

# 4. 전체 통합 테스트
cd ..
./gradlew test                               # 전체 프로젝트 테스트
```

### 📅 릴리즈 워크플로우

#### 버전 배포 프로세스
```bash
# 1. 릴리즈 브랜치 생성
git checkout -b release/v1.0.0

# 2. 버전 정보 업데이트
# build.gradle.kts
version = "1.0.0"  # SNAPSHOT 제거

# 3. 전체 빌드 및 테스트
./gradlew clean build
./gradlew test

# 4. 배포용 JAR 생성
./gradlew :tetris-client:bootJar
ls -la tetris-client/build/libs/
# tetris-desktop-app-java21-1.0.0.jar 생성 확인

# 5. 실행 테스트
java -jar tetris-client/build/libs/tetris-desktop-app-java21-1.0.0.jar

# 6. 릴리즈 태그 생성
git tag v1.0.0
git push origin v1.0.0
```

---

## 8. 모듈별 독립 개발 가이드

### 🎯 tetris-core 모듈 독립 개발

#### 독립 빌드 및 테스트
```bash
# Core 모듈로 이동
cd tetris-core

# 독립 빌드
../gradlew :tetris-core:build

# 독립 테스트 실행
../gradlew :tetris-core:test

# 특정 테스트만 실행
../gradlew :tetris-core:test --tests "*TetrisBoardTest"

# 지속적 테스트 (파일 변경 시 자동 재실행)
../gradlew :tetris-core:test --continuous
```

#### Core 모듈 독립 테스트 예시
```java
// src/test/java/seoultech/se/core/model/TetrisBoardTest.java
public class TetrisBoardTest {
    
    @Test
    @DisplayName("블록 배치 가능성 테스트")
    void testCanPlaceBlock() {
        // Given: 순수 Java 테스트 - 외부 의존성 없음
        TetrisBoard board = new TetrisBoard();
        TetrisBlockType blockType = TetrisBlockType.I;
        
        // When: 보드 중앙에 블록 배치 시도
        boolean canPlace = board.canPlaceBlock(blockType.getShape(), 5, 0);
        
        // Then: 배치 가능해야 함
        assertTrue(canPlace);
    }
    
    @Test
    @DisplayName("라인 제거 테스트")
    void testLineClear() {
        TetrisBoard board = new TetrisBoard();
        
        // 가상의 가득찬 라인 생성
        // ... 라인 설정 로직
        
        int clearedLines = board.clearCompletedLines();
        assertEquals(1, clearedLines);
    }
}

// 실행: ../gradlew :tetris-core:test
// 결과: 순수 Java 로직만 테스트, 매우 빠름
```

#### Core 모듈 JAR 배포
```bash
# JAR 생성
../gradlew :tetris-core:jar

# 생성된 JAR 확인
ls -la build/libs/
# tetris-core-1.0.0-SNAPSHOT.jar

# JAR 내용 확인
jar tf build/libs/tetris-core-1.0.0-SNAPSHOT.jar
# seoultech/se/core/model/TetrisBoard.class
# seoultech/se/core/model/TetrisBlockType.class
# seoultech/se/core/concurrent/TetrisGameThreadManager.class
```

### ⚙️ tetris-backend 모듈 독립 개발

#### Spring Boot 독립 실행
```bash
# Backend 모듈로 이동
cd tetris-backend

# Spring Boot 애플리케이션으로 독립 실행 (테스트용)
../gradlew :tetris-backend:bootRun

# 또는 테스트만 실행
../gradlew :tetris-backend:test

# 특정 서비스 테스트
../gradlew :tetris-backend:test --tests "*ScoreServiceTest"
```

#### Backend 모듈 독립 테스트 예시
```java
// src/test/java/seoultech/se/backend/service/ScoreServiceTest.java
@SpringBootTest(classes = ScoreService.class)  // 최소한의 Spring 컨텍스트
class ScoreServiceTest {
    
    @Autowired
    private ScoreService scoreService;
    
    @Test
    @DisplayName("점수 계산 테스트")
    void testScoreCalculation() {
        // Given: 기본 레벨 1
        scoreService.resetGame();
        
        // When: 4라인 클리어 (Tetris!)
        scoreService.addScore(4);
        
        // Then: 800점 (기본 800 * 레벨 1)
        assertEquals(800, scoreService.getCurrentScore());
        assertEquals(1, scoreService.getCurrentLevel());
    }
    
    @Test
    @DisplayName("레벨업 테스트")
    void testLevelUp() {
        scoreService.resetGame();
        
        // 10라인 클리어로 레벨업
        for (int i = 0; i < 10; i++) {
            scoreService.addScore(1);
        }
        
        assertEquals(2, scoreService.getCurrentLevel());
    }
}

// 실행: ../gradlew :tetris-backend:test
// 결과: Spring Boot 컨텍스트와 함께 테스트
```

#### Backend 모듈 독립 서비스 체크
```java
// 간단한 테스트 애플리케이션 생성
// src/test/java/TestRunner.java
@SpringBootApplication
public class TestRunner {
    
    @Autowired
    private ScoreService scoreService;
    
    public static void main(String[] args) {
        SpringApplication.run(TestRunner.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void runTest() {
        System.out.println("📊 Backend 서비스 독립 테스트 시작");
        
        scoreService.addScore(4);
        System.out.println("점수: " + scoreService.getCurrentScore());
        System.out.println("레벨: " + scoreService.getCurrentLevel());
        
        System.out.println("✅ Backend 모듈 정상 동작!");
    }
}

// 실행: ../gradlew :tetris-backend:bootRun
```

### 🖥️ tetris-client 모듈 독립 개발

#### Client 모듈 독립 실행
```bash
# Client 모듈로 이동
cd tetris-client

# JavaFX + Spring Boot 애플리케이션 실행
../gradlew :tetris-client:bootRun

# 또는 JAR 생성 후 실행
../gradlew :tetris-client:bootJar
java -jar build/libs/tetris-desktop-app-java21-1.0.0-SNAPSHOT.jar

# 개발자 모드
../gradlew :tetris-client:dev
```

#### Client UI 모크업 개발
```java
// MainController에 모크 데이터 추가
@Component
public class MainController implements Initializable {
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 모크 데이터로 UI 개발
        setupMockData();
    }
    
    private void setupMockData() {
        // Backend 서비스 없이도 UI 개발 가능
        scoreLabel.setText("점수: 12,300");
        levelLabel.setText("레벨: 5");
        linesLabel.setText("라인: 47");
        
        // Core 모듈은 실제 데이터 사용
        TetrisBlockType randomBlock = TetrisBlockType.getRandomType();
        drawBlock(randomBlock);
    }
}
```

#### 모듈별 Hot Reload 개발
```bash
# Terminal 1: Core 모듈 지속적 빌드
cd tetris-core
../gradlew :tetris-core:build --continuous

# Terminal 2: Backend 모듈 지속적 빌드  
cd tetris-backend
../gradlew :tetris-backend:build --continuous

# Terminal 3: Client 애플리케이션 실행
cd tetris-client
../gradlew :tetris-client:bootRun

# 결과: 코드 수정 → 자동 빌드 → 애플리케이션 재시작
```

### 🔄 모듈 간 통합 테스트

#### 단계별 통합 테스트
```bash
# 1단계: Core + Backend 통합
cd tetris-backend
../gradlew :tetris-backend:test
# 결과: ScoreService에서 TetrisBoard 사용 기능 테스트

# 2단계: Backend + Client 통합
cd tetris-client
../gradlew :tetris-client:test  
# 결과: MainController에서 ScoreService 주입 테스트

# 3단계: 전체 통합
cd ..
./gradlew test
# 결과: 모든 모듈 통합 테스트

# 4단계: 실제 애플리케이션 실행
./gradlew :tetris-client:bootRun
# 결과: 전체 시스템 통합 실행
```

#### 모듈 간 API 계약 테스트
```java
// 모듈 간 인터페이스 계약 테스트
@SpringBootTest
class ModuleIntegrationTest {
    
    @Autowired
    private ScoreService scoreService;  // Backend 모듈
    
    @Test
    @DisplayName("Core-Backend 모듈 통합 테스트")
    void testCoreBackendIntegration() {
        // Core 모듈의 도메인 객체 사용
        TetrisBoard board = new TetrisBoard();
        TetrisBlockType blockType = TetrisBlockType.I;
        
        // Backend 서비스에서 Core 로직 활용 테스트
        scoreService.resetGame();
        
        // 시나리오: 블록 배치 및 라인 클리어
        boolean placed = board.placeBlock(blockType.getShape(), 5, 0, 1);
        assertTrue(placed);
        
        int clearedLines = board.clearCompletedLines();
        scoreService.addScore(clearedLines);
        
        // 통합 결과 검증
        assertTrue(scoreService.getCurrentScore() >= 0);
    }
}
```

---

## 9. 패키지 구조 및 네이밍 컨벤션

### 📁 전체 패키지 구조
```
seoultech.se/
├── core/                    # tetris-core 모듈
│   ├── model/              # 도메인 모델
│   │   ├── TetrisBoard
│   │   ├── TetrisBlockType
│   │   └── GameState
│   ├── concurrent/         # 동시성 처리
│   │   └── TetrisGameThreadManager
│   └── algorithm/          # 게임 알고리즘 (향후 확장)
│       ├── BlockRotation
│       └── LineClearing
│
├── backend/                # tetris-backend 모듈
│   ├── service/           # 비즈니스 서비스
│   │   ├── ScoreService
│   │   ├── GameService     # (향후 추가)
│   │   └── SettingsService # (향후 추가)
│   ├── config/           # Spring 설정
│   │   └── GameConfiguration
│   └── dto/              # 데이터 전송 객체
│       └── GameStatsDto
│
└── client/               # tetris-client 모듈
    ├── TetrisApplication # 메인 애플리케이션
    ├── controller/       # JavaFX 컨트롤러
    │   ├── MainController
    │   ├── MenuController     # (향후 추가)
    │   └── SettingsController # (향후 추가)
    ├── view/            # 커스텀 JavaFX 컴포넌트
    │   ├── TetrisCanvas
    │   └── GameBoard
    └── util/            # 클라이언트 유틸리티
        ├── KeyboardHandler
        └── GraphicsUtil
```

### 🏷️ 네이밍 컨벤션

#### 클래스 네이밍
| 타입 | 패턴 | 예시 |
|------|------|------|
| Domain Model | `명사` | `TetrisBoard`, `TetrisBlockType` |
| Service | `명사 + Service` | `ScoreService`, `GameService` |
| Controller | `명사 + Controller` | `MainController`, `MenuController` |
| Util | `기능 + Util/Helper` | `GraphicsUtil`, `KeyboardHandler` |
| Manager | `기능 + Manager` | `TetrisGameThreadManager` |

#### 메서드 네이밍
| 목적 | 패턴 | 예시 |
|------|------|------|
| 조회 | `get + 명사` | `getCurrentScore()`, `getBoardState()` |
| 상태 확인 | `is/can/has + 형용사` | `isGameOver()`, `canPlaceBlock()` |
| 액션 수행 | `동사` | `placeBlock()`, `clearLines()`, `resetGame()` |
| 계산/생성 | `calculate/generate + 명사` | `calculateScore()`, `generateNewBlock()` |

#### 상수 네이밍
```java
// 대문자 + 언더스코어
public static final int BOARD_WIDTH = 10;
public static final int TETRIS_LINE_SCORE = 800;
public static final Duration DEFAULT_DROP_INTERVAL = Duration.ofMillis(1000);
```

---

## 10. 개발 가이드라인

### 🎯 Core 모듈 개발 원칙

#### ✅ DO (권장)
```java
// ✅ 순수 Java 사용
public class TetrisBoard {
    private final int[][] board;  // 불변 참조
    
    public boolean canPlaceBlock(int[][] shape, int x, int y) {
        // 외부 의존성 없는 순수 로직
        return validatePosition(shape, x, y);
    }
}

// ✅ 불변성 보장
public enum TetrisBlockType {
    I("I", new int[][]{{1,1,1,1}}, "#00FFFF");
    
    public int[][] getShape() {
        return deepCopy(shape);  // 복사본 반환
    }
}
```

#### ❌ DON'T (금지)
```java
// ❌ Spring 의존성 사용 금지
@Service  // Core 모듈에서 사용 불가
public class TetrisBoard { ... }

// ❌ JavaFX 의존성 사용 금지
public class BlockRenderer {
    private Canvas canvas;  // JavaFX 클래스 사용 불가
}

// ❌ 가변 상태 노출 금지
public int[][] getBoard() {
    return board;  // 원본 배열 노출 위험
}
```

### ⚙️ Backend 모듈 개발 원칙

#### ✅ DO (권장)
```java
// ✅ Spring 어노테이션 활용
@Service
@Transactional
public class ScoreService {
    
    // ✅ Thread-Safe 구현
    private final AtomicLong score = new AtomicLong();
    
    // ✅ Core 모듈 활용
    public void processGameTurn(TetrisBoard board) {
        int clearedLines = board.clearCompletedLines();
        addScore(clearedLines);
    }
}

// ✅ 명확한 책임 분리
@Service
public class GameService {        // 게임 진행 관리
@Service  
public class SettingsService {   // 설정 관리
@Service
public class StatisticsService { // 통계 관리
```

#### ❌ DON'T (금지)
```java
// ❌ JavaFX 의존성 사용 금지
@Service
public class UIService {
    private Stage primaryStage;  // JavaFX 클래스 사용 불가
}

// ❌ Thread-Safe하지 않은 구현
@Service
public class ScoreService {
    private int score;  // 동시성 문제 발생 가능
    
    public void addScore(int points) {
        score += points;  // Race Condition 위험
    }
}
```

### 🖥️ Client 모듈 개발 원칙

#### ✅ DO (권장)
```java
// ✅ Spring Component로 등록
@Component
public class MainController implements Initializable {
    
    // ✅ 서비스 의존성 주입
    @Autowired
    private ScoreService scoreService;
    
    // ✅ Core 모듈 직접 활용
    private void handleBlockRotation() {
        int[][] rotated = currentBlock.getRotatedShape();
        updateCanvas(rotated);
    }
    
    // ✅ UI 업데이트는 JavaFX Thread에서
    private void updateScore() {
        Platform.runLater(() -> {
            scoreLabel.setText("점수: " + scoreService.getCurrentScore());
        });
    }
}
```

#### ❌ DON'T (금지)
```java
// ❌ new 키워드로 서비스 생성 금지
public class MainController {
    private ScoreService scoreService = new ScoreService();  // DI 사용해야 함
}

// ❌ JavaFX Thread 외부에서 UI 조작 금지
public void updateUI() {
    scoreLabel.setText("...");  // IllegalStateException 발생 가능
}

// ❌ 비즈니스 로직을 Controller에 구현 금지
public class MainController {
    public void calculateScore(int lines) {
        // 복잡한 점수 계산 로직... 
        // → ScoreService로 이동해야 함
    }
}
```

### 🧪 테스트 전략

#### Core 모듈 테스트
```java
// ✅ 순수 Java 단위 테스트
@Test
void testBlockPlacement() {
    TetrisBoard board = new TetrisBoard();
    TetrisBlockType block = TetrisBlockType.I;
    
    boolean canPlace = board.canPlaceBlock(block.getShape(), 5, 0);
    assertTrue(canPlace);
}
```

#### Backend 모듈 테스트
```java
// ✅ Spring Boot 통합 테스트
@SpringBootTest
class ScoreServiceTest {
    @Autowired
    private ScoreService scoreService;
    
    @Test
    void testScoreCalculation() {
        scoreService.addScore(4);  // Tetris
        assertEquals(800, scoreService.getCurrentScore());
    }
}
```

#### Client 모듈 테스트
```java
// ✅ JavaFX + Spring 통합 테스트
@SpringBootTest
@ExtendWith(JavaFXExtension.class)
class MainControllerTest {
    @MockBean
    private ScoreService scoreService;
    
    @Test
    void testUIUpdate() {
        // JavaFX 컴포넌트 테스트
    }
}
```

### 🔧 빌드 및 실행

#### 개발 모드
```bash
# 전체 빌드
./gradlew clean build

# 개발 모드 실행 (Hot Reload)
./gradlew :tetris-client:bootRun

# 특정 모듈 테스트
./gradlew :tetris-core:test
./gradlew :tetris-backend:test
```

#### 배포 모드
```bash
# JAR 빌드
./gradlew :tetris-client:bootJar

# JAR 실행
java -jar tetris-client/build/libs/tetris-desktop-app-java21-1.0.0-SNAPSHOT.jar

# 간편 스크립트
./run-tetris.sh
```

---

## 📚 요약 및 참조

### 🎯 핵심 아키텍처 요약

#### 멀티모듈 구조
```
tetris-app (Java 21 LTS)
├── tetris-core      🎯 순수 Java 도메인 로직
│   ├── TetrisBoard: 게임 보드 로직
│   ├── TetrisBlockType: 블록 정의 및 회전
│   └── TetrisGameThreadManager: Virtual Threads
│
├── tetris-backend   ⚙️ Spring Boot 서비스 레이어
│   └── ScoreService: 점수 및 레벨 관리
│
└── tetris-client    🖥️ JavaFX + Spring Boot 메인 앱
    ├── TetrisApplication: Spring + JavaFX 통합
    └── MainController: 게임 컨트롤러
```

#### 통합 체계
- **빌드**: Gradle 멀티모듈 → 의존성 순서대로 빌드 → Spring Boot JAR
- **실행**: JarLauncher → Spring 컨텍스트 → JavaFX 화면 → 사용자 상호작용
- **통신**: Client ↔ Backend (DI) ↔ Core (직접 호출)

#### 개발 워크플로우
1. **모듈별 독립 개발**: 각 모듈을 독립적으로 개발 및 테스트
2. **점진적 통합**: Core → Backend → Client 순서로 통합
3. **지속적 테스트**: 모듈별 테스트 → 통합 테스트 → 전체 시스템 테스트

### 🛠️ 주요 명령어 참조

#### 개발 명령어
```bash
# 전체 프로젝트
./gradlew clean build                    # 전체 빌드
./gradlew test                          # 전체 테스트
./verify-gradle-setup.sh                # 설정 검증

# 모듈별 개발
./gradlew :tetris-core:test             # Core 테스트
./gradlew :tetris-backend:test          # Backend 테스트
./gradlew :tetris-client:bootRun        # Client 실행

# 배포
./gradlew :tetris-client:bootJar        # 실행 가능 JAR
./run-tetris.sh                         # 간편 실행
./build-and-run.sh                      # 빌드 + 실행
```

#### 독립 개발 패턴
```bash
# 모듈별 독립 작업
cd tetris-core && ../gradlew :tetris-core:test --continuous
cd tetris-backend && ../gradlew :tetris-backend:bootRun
cd tetris-client && ../gradlew :tetris-client:dev

# Hot Reload 개발
# Terminal 1: Core 지속 빌드
# Terminal 2: Backend 지속 빌드
# Terminal 3: Client 애플리케이션 실행
```

### 🎮 아키텍처의 장점

#### ✅ **모듈화의 이점**
- **독립 개발**: 각 팀이 다른 모듈을 동시에 개발 가능
- **테스트 격리**: 모듈별로 독립적인 테스트 가능
- **재사용성**: Core 모듈을 다른 UI에서 재사용 가능
- **유지보수**: 각 모듈의 책임이 명확하여 수정 영향도 최소화

#### ✅ **Spring + JavaFX 통합의 이점**
- **완전한 DI**: JavaFX 컨트롤러에서 Spring 서비스 자동 주입
- **생태계 활용**: Spring Boot의 모든 기능 활용 가능
- **테스트 용이성**: @MockBean 등 Spring 테스트 도구 활용
- **확장성**: 새로운 서비스나 기능 추가가 쉬움

#### ✅ **Gradle 멀티모듈의 이점**
- **점진적 빌드**: 변경된 모듈만 재빌드
- **병렬 빌드**: 독립적인 모듈은 병렬로 빌드
- **의존성 관리**: 모듈 간 의존성 자동 해결
- **배포 최적화**: 최종 JAR에 필요한 것만 포함

### 🚀 확장 가능성

#### 새 모듈 추가 예시
```
tetris-app/
├── tetris-core      # 기존
├── tetris-backend   # 기존
├── tetris-client    # 기존
├── tetris-ai        # 새 모듈: AI 플레이어
├── tetris-network   # 새 모듈: 멀티플레이어
└── tetris-web       # 새 모듈: 웹 인터페이스
```

#### 새 UI 프레임워크 포팅
```java
// Core + Backend 모듈 재사용
// 새로운 UI만 개발
tetris-android/     // Android 앱
tetris-ios/         // iOS 앱
tetris-web/         // React/Vue 웹앱
tetris-console/     // 콘솔 게임
```

### 🔗 관련 문서
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [JavaFX Documentation](https://openjfx.io/openjfx-docs/)
- [Java 21 Virtual Threads](https://openjdk.org/jeps/444)

### 🛠️ 개발 도구
- **IDE**: IntelliJ IDEA (권장), VS Code
- **Java**: OpenJDK 21 LTS
- **Build**: Gradle 8.5
- **GUI**: Scene Builder (FXML 편집용)

### 📈 성능 모니터링
```java
// Virtual Thread 모니터링
TetrisGameThreadManager manager = new TetrisGameThreadManager();
manager.printThreadInfo();

// 메모리 사용량 체크
Runtime runtime = Runtime.getRuntime();
long usedMemory = runtime.totalMemory() - runtime.freeMemory();
System.out.println("사용 메모리: " + usedMemory / 1024 / 1024 + "MB");
```

---

**✨ 이 아키텍처를 통해 유지보수가 쉽고, 테스트 가능하며, 확장성 있는 테트리스 게임을 개발할 수 있습니다!**
