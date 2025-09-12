# Bug Report: GUI 애플리케이션 Docker 실행 시 HeadlessException 발생

🐞 **이슈 요약:** `javax.swing` 기반의 GUI 애플리케이션을 Docker 컨테이너로 실행할 때, 화면(Display)이 없는 환경으로 인해 `java.awt.HeadlessException`이 발생하며 즉시 종료됩니다.

### 오류 원인

Java의 GUI 프레임워크인 AWT/Swing은 애플리케이션 창을 화면에 그리기 위해 OS의 그래픽 시스템(예: Linux의 X11)과의 연결을 필요로 합니다. 하지만 기본 Docker 컨테이너는 서버 환경처럼 모니터, 키보드, 마우스가 없는 **'헤드리스(Headless)' 환경**입니다.

이러한 환경에서 Swing 컴포넌트(`JFrame` 등)를 생성하려고 하면, 프로그램이 연결할 수 있는 화면이 없다는 것을 감지하고 `HeadlessException`을 발생시켜 비정상 종료됩니다. 이는 코드 자체의 버그라기보다는, **GUI를 필요로 하는 애플리케이션을 GUI가 없는 환경에서 실행**하려고 했기 때문에 발생하는 환경적 문제입니다.

### 재현 환경

* **운영체제:** Linux, macOS, Windows (WSL2) 등 Docker를 실행할 수 있는 모든 OS
* **런타임:** Docker Engine
* **애플리케이션:** Java 11 기반 Swing GUI 애플리케이션 (Tetris)
* **빌드 도구:** Gradle
* **Docker 베이스 이미지:** `openjdk:11-jdk-slim` (헤드리스 환경)

---

## Bug Report 양식

### **Title: GUI 애플리케이션 Docker 실행 시 `java.awt.HeadlessException` 발생**

#### **Description**
`./gradlew shadowJar`를 통해 빌드된 Tetris GUI 애플리케이션을 Docker 컨테이너로 실행하면, GUI를 렌더링할 Display를 찾지 못해 `java.awt.HeadlessException`이 발생하며 컨테이너가 즉시 종료됩니다.

#### **Steps to Reproduce (재현 단계)**
1.  프로젝트를 `shadowJar`를 이용해 빌드합니다.
    ```bash
    ./gradlew clean shadowJar
    ```
2.  아래 내용으로 `Dockerfile`을 작성합니다.
    ```dockerfile
    FROM openjdk:11-jdk-slim
    WORKDIR /app
    COPY build/libs/*-all.jar app.jar
    ENTRYPOINT ["java", "-jar", "app.jar"]
    ```
3.  Docker 이미지를 빌드합니다.
    ```bash
    docker build -t tetris-app .
    ```
4.  Docker 컨테이너를 실행합니다.
    ```bash
    docker run --rm tetris-app
    ```

#### **Expected Result (기대 결과)**
Host 머신의 화면에 Tetris 게임 창이 정상적으로 나타난다.

#### **Actual Result (실제 결과)**
컨테이너가 즉시 종료되며, 아래와 같은 `HeadlessException` 스택 트레이스를 출력한다.
```
Exception in thread "main" java.awt.HeadlessException:
No X11 DISPLAY variable was set, but this program performed an operation which requires it.
at java.desktop/java.awt.GraphicsEnvironment.checkHeadless(GraphicsEnvironment.java:208)
at java.desktop/java.awt.Window.<init>(Window.java:548)
at java.desktop/java.awt.Frame.<init>(Frame.java:423)
at java.desktop/javax.swing.JFrame.<init>(JFrame.java:224)
at seoultech.se.tetris.component.Board.<init>(Board.java:50)
at seoultech.se.tetris.main.Tetris.main(Tetris.java:8)
```

---

## 해결 방법

✅ 이 문제는 Docker 컨테이너가 Host OS의 Display Server에 접근할 수 있도록 **실행 옵션을 추가**하여 해결할 수 있습니다.

### **해결 방안**
`docker run` 명령어 실행 시, Host의 Display 정보를 컨테이너에 전달하는 환경 변수(`-e`)와 통신 소켓을 공유하는 볼륨(`-v`) 옵션을 추가합니다.

#### **Linux 환경**
```bash
docker run --rm \
    -e DISPLAY=$DISPLAY \
    -v /tmp/.X11-unix:/tmp/.X11-unix \
    tetris-app
```

### macOS 환경 (XQuartz 설치 필요)
```bash
export DISPLAY=$(ipconfig getifaddr en0):0
```
docker run --rm \
    -e DISPLAY=host.docker.internal:0 \
    tetris-app
```
### Windows 환경 (VcXsrv 설치 필요)
```
docker run --rm \
    -e DISPLAY=host.docker.internal:0.0 \
    tetris-app
```

docker-compose.yml 적용 방안 (Linux 기준)
지속적인 관리를 위해 docker-compose.yml에 해당 설정을 명시할 수 있습니다.

```yml
version: '3.8'
services:
  tetris-app:
    build: ..
    environment:
      - DISPLAY=${DISPLAY} # Host의 DISPLAY 환경 변수 전달
    volumes:
      - /tmp/.X11-unix:/tmp/.X11-unix # X11 소켓 파일 공유
```