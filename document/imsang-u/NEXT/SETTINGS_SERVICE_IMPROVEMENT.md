## 🔧 SettingsService 개선 제안

### 현재 코드의 문제점

```java
@Service
public class SettingsService {
    private Stage primaryStage;  // ❌ UI 의존성
    private static final String SETTINGS_FILE = "tetris_settings";  // ❌ 경로 하드코딩
    
    public void loadSettings() {
        try (FileInputStream in = new FileInputStream(new File(SETTINGS_FILE))) {
            // ❌ 예외 처리가 너무 포괄적
            props.load(in);
        } catch (Exception e) {  // ❌ Exception은 너무 넓음
            System.out.println("❗ Failed to load settings, using defaults.");
        }
    }
}
```

### 문제점 상세

1. **UI 의존성**: `Stage` 객체를 직접 보유
   - 서비스 레이어가 UI에 의존하면 테스트 어려움
   - 콘솔 모드나 다른 UI 프레임워크 사용 불가

2. **파일 경로 하드코딩**
   - 운영체제별 적절한 위치 미사용
   - 상대 경로로 저장하면 실행 위치에 따라 달라짐

3. **예외 처리 불충분**
   - `Exception`은 너무 포괄적
   - `IOException`, `NumberFormatException` 등 구분 필요

4. **로깅 부재**
   - `System.out.println` 대신 Logger 사용 필요
   - 디버깅 및 모니터링 어려움

5. **설정 검증 부재**
   - 잘못된 값 검증 없음 (음수 볼륨, 잘못된 해상도 등)

### 개선된 코드

```java
@Service
@Slf4j  // Lombok 로깅
public class SettingsService {
    
    // UI 의존성 제거 - Event 발행으로 대체
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    // 설정 파일 경로 개선
    private static final String SETTINGS_DIR = System.getProperty("user.home") 
        + File.separator + ".tetris";
    private static final String SETTINGS_FILE = SETTINGS_DIR 
        + File.separator + "settings.properties";
    
    // JavaFX Properties
    private final DoubleProperty soundVolume = new SimpleDoubleProperty(80);
    private final StringProperty colorMode = new SimpleStringProperty("default");
    private final DoubleProperty stageWidth = new SimpleDoubleProperty(500);
    private final DoubleProperty stageHeight = new SimpleDoubleProperty(700);
    
    // 설정 제약사항
    private static final double MIN_VOLUME = 0.0;
    private static final double MAX_VOLUME = 100.0;
    private static final double MIN_WIDTH = 400.0;
    private static final double MIN_HEIGHT = 500.0;
    
    public SettingsService() {
        ensureSettingsDirectoryExists();
        loadSettings();
    }
    
    /**
     * 설정 디렉토리 생성
     */
    private void ensureSettingsDirectoryExists() {
        File dir = new File(SETTINGS_DIR);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                log.info("Created settings directory: {}", SETTINGS_DIR);
            } else {
                log.error("Failed to create settings directory: {}", SETTINGS_DIR);
            }
        }
    }
    
    /**
     * 설정 로드 (개선된 예외 처리)
     */
    public void loadSettings() {
        File file = new File(SETTINGS_FILE);
        
        if (!file.exists()) {
            log.info("Settings file not found, using defaults");
            restoreDefaults();
            return;
        }
        
        Properties props = new Properties();
        
        try (FileInputStream in = new FileInputStream(file)) {
            props.load(in);
            
            // 각 설정값 파싱 및 검증
            loadVolumeSetting(props);
            loadColorModeSetting(props);
            loadResolutionSettings(props);
            
            log.info("Settings loaded successfully from: {}", SETTINGS_FILE);
            
        } catch (IOException e) {
            log.error("Failed to load settings from file", e);
            restoreDefaults();
        }
    }
    
    /**
     * 볼륨 설정 로드 (검증 포함)
     */
    private void loadVolumeSetting(Properties props) {
        try {
            double volume = Double.parseDouble(
                props.getProperty("soundVolume", "80")
            );
            
            // 범위 검증
            if (volume < MIN_VOLUME || volume > MAX_VOLUME) {
                log.warn("Invalid volume value: {}, using default", volume);
                soundVolume.set(80);
            } else {
                soundVolume.set(volume);
            }
            
        } catch (NumberFormatException e) {
            log.error("Invalid volume format, using default", e);
            soundVolume.set(80);
        }
    }
    
    /**
     * 색상 모드 설정 로드 (검증 포함)
     */
    private void loadColorModeSetting(Properties props) {
        String mode = props.getProperty("colorMode", "default");
        
        // 유효한 색상 모드 목록
        Set<String> validModes = Set.of(
            "default", "rg_blind", "yb_blind"
        );
        
        if (validModes.contains(mode)) {
            colorMode.set(mode);
        } else {
            log.warn("Invalid color mode: {}, using default", mode);
            colorMode.set("default");
        }
    }
    
    /**
     * 해상도 설정 로드 (검증 포함)
     */
    private void loadResolutionSettings(Properties props) {
        try {
            double width = Double.parseDouble(
                props.getProperty("stageWidth", "500")
            );
            double height = Double.parseDouble(
                props.getProperty("stageHeight", "700")
            );
            
            // 최소값 검증
            if (width < MIN_WIDTH || height < MIN_HEIGHT) {
                log.warn("Invalid resolution: {}x{}, using default", 
                    width, height);
                applyResolution(500, 700);
            } else {
                applyResolution(width, height);
            }
            
        } catch (NumberFormatException e) {
            log.error("Invalid resolution format, using default", e);
            applyResolution(500, 700);
        }
    }
    
    /**
     * 해상도 적용 (Event 발행으로 변경)
     */
    public void applyResolution(double width, double height) {
        stageWidth.set(width);
        stageHeight.set(height);
        
        // UI 의존성 제거 - Event 발행
        eventPublisher.publishEvent(
            new ResolutionChangedEvent(this, width, height)
        );
        
        log.debug("Resolution changed to: {}x{}", width, height);
    }
    
    /**
     * 설정 저장 (개선된 예외 처리)
     */
    public void saveSettings() {
        Properties props = new Properties();
        
        props.setProperty("soundVolume", String.valueOf(soundVolume.get()));
        props.setProperty("colorMode", colorMode.get());
        props.setProperty("stageWidth", String.valueOf(stageWidth.get()));
        props.setProperty("stageHeight", String.valueOf(stageHeight.get()));
        
        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            props.store(out, "Tetris Game Settings");
            log.info("Settings saved successfully to: {}", SETTINGS_FILE);
            
        } catch (IOException e) {
            log.error("Failed to save settings to file", e);
            // UI에 에러 알림 (Event 발행)
            eventPublisher.publishEvent(
                new SettingsSaveFailedEvent(this, e.getMessage())
            );
        }
    }
    
    /**
     * 볼륨 설정 (검증 포함)
     */
    public void setSoundVolume(double volume) {
        if (volume < MIN_VOLUME || volume > MAX_VOLUME) {
            throw new IllegalArgumentException(
                "Volume must be between " + MIN_VOLUME + " and " + MAX_VOLUME
            );
        }
        soundVolume.set(volume);
        saveSettings();
    }
    
    // Property getters
    public DoubleProperty soundVolumeProperty() { return soundVolume; }
    public StringProperty colorModeProperty() { return colorMode; }
    public DoubleProperty stageWidthProperty() { return stageWidth; }
    public DoubleProperty stageHeightProperty() { return stageHeight; }
}
```

### Event 클래스 추가

```java
/**
 * 해상도 변경 이벤트
 */
@Getter
@AllArgsConstructor
public class ResolutionChangedEvent extends ApplicationEvent {
    private final double width;
    private final double height;
    
    public ResolutionChangedEvent(Object source, double width, double height) {
        super(source);
        this.width = width;
        this.height = height;
    }
}

/**
 * 설정 저장 실패 이벤트
 */
@Getter
public class SettingsSaveFailedEvent extends ApplicationEvent {
    private final String errorMessage;
    
    public SettingsSaveFailedEvent(Object source, String errorMessage) {
        super(source);
        this.errorMessage = errorMessage;
    }
}
```

### 개선 사항 요약

1. ✅ **UI 의존성 제거**: Event 발행으로 대체
2. ✅ **파일 경로 개선**: 사용자 홈 디렉토리 사용
3. ✅ **예외 처리 정교화**: IOException, NumberFormatException 구분
4. ✅ **로깅 추가**: SLF4J + Lombok
5. ✅ **설정 검증**: 범위 체크, 유효성 검증
6. ✅ **에러 처리**: 사용자에게 알림 제공
7. ✅ **테스트 용이성**: Mock 가능한 구조

### 테스트 코드 예시

```java
@SpringBootTest
class SettingsServiceTest {
    
    @Autowired
    private SettingsService settingsService;
    
    @MockBean
    private ApplicationEventPublisher eventPublisher;
    
    @Test
    void 볼륨_범위_검증_테스트() {
        // Given
        double invalidVolume = 150.0;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            settingsService.setSoundVolume(invalidVolume);
        });
    }
    
    @Test
    void 해상도_변경_이벤트_발행_테스트() {
        // Given
        double width = 800;
        double height = 600;
        
        // When
        settingsService.applyResolution(width, height);
        
        // Then
        verify(eventPublisher).publishEvent(
            any(ResolutionChangedEvent.class)
        );
    }
}
```
