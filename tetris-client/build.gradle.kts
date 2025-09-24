/*
 * Tetris Client Module (JavaFX Desktop Application)
 * 🖥️ JavaFX 21 LTS + Spring Boot DI Container 통합
 * - JavaFX가 메인 애플리케이션 (GUI)
 * - Spring Boot는 서비스 레이어 (DI 컨테이너)
 * - Java 21의 Virtual Threads, 향상된 concurrent 기능 활용
 */
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management") 
    id("org.openjfx.javafxplugin")
    application  // JavaFX 애플리케이션
}

// 🌱 Spring Boot 설정
springBoot {
    mainClass = "seoultech.se.client.TetrisApplication"
}

description = "Tetris JavaFX Desktop Application with Java 21 LTS"

// 🎮 JavaFX 21 LTS 설정 (Java 21과 완벽 호환)
javafx {
    version = "21"
    modules = listOf(
        "javafx.controls",
        "javafx.fxml"
    )
}

// 🚀 메인 애플리케이션 설정
application {
    mainClass.set("seoultech.se.client.TetrisApplication")
}

dependencies {
    // 🎯 핵심 모듈 직접 의존성 (POJO 알고리즘 직접 사용)
    implementation(project(":tetris-core"))
    
    // ⚙️ 백엔드 서비스 의존성 (서비스 레이어)
    implementation(project(":tetris-backend"))
    
    // 🌱 Spring Boot 핵심 (웹 기능 제외)
    implementation("org.springframework.boot:spring-boot-starter")
    
    // 🎨 JavaFX 21 LTS 핵심 의존성 (기본적인 기능만)
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    
    // ⚙️ 설정 관리
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    
    // 🔧 개발 도구 (JavaFX와 충돌할 수 있으므로 주석처리)
    // developmentOnly("org.springframework.boot:spring-boot-devtools")
    
    // 📊 유틸리티 (기본적인 기능만)
    implementation("org.apache.commons:commons-lang3:3.17.0")
    
    // 🧪 테스트 (기본적인 기능만)
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// 🚀 실행 설정 (JavaFX + Java 21 Virtual Threads 최적화)
val javafxJvmArgs = listOf(
    // JavaFX 모듈 접근 허용
    "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
    "--add-opens", "javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED",
    "--add-opens", "javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED",
    "--add-opens", "javafx.base/com.sun.javafx.binding=ALL-UNNAMED",
    "--add-opens", "javafx.base/com.sun.javafx.event=ALL-UNNAMED",
    
    // Spring Boot 리플렉션 지원
    "--add-opens", "java.base/java.lang=ALL-UNNAMED",
    "--add-opens", "java.base/java.util=ALL-UNNAMED",
    "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED",
    
    // Desktop 앱 최적화
    "-Dprism.order=sw",
    "-Dprism.text=t2k"
)

tasks.run.configure {
    jvmArgs(javafxJvmArgs)
}

// Spring Boot 실행을 위한 설정
tasks.bootRun.configure {
    jvmArgs(javafxJvmArgs)
    
    // JavaFX 런타임을 명시적으로 추가
    doFirst {
        val javaFxVersion = "21"
        val platform = org.gradle.internal.os.OperatingSystem.current()
        val osName = when {
            platform.isLinux -> "linux"
            platform.isMacOsX -> "mac"
            platform.isWindows -> "win"
            else -> throw GradleException("Unsupported OS: ${platform.name}")
        }
        
        systemProperty("javafx.runtime.path", "${gradle.gradleUserHomeDir}/caches/modules-2/files-2.1")
    }
}

// 📦 실행 가능한 JAR 설정
tasks.bootJar {
    archiveBaseName.set("tetris-desktop-app-java21")
    enabled = true
    
    // Spring Boot 자동 Main-Class 설정 사용
    // Spring Boot가 자동으로 JarLauncher를 Main-Class로 설정
    manifest {
        attributes(
            "Implementation-Title" to "Tetris Desktop Game (Java 21 LTS)",
            "Implementation-Version" to project.version,
            "Add-Opens" to "javafx.graphics/com.sun.javafx.application javafx.controls/com.sun.javafx.scene.control.behavior"
        )
    }
}

// 🧪 테스트 설정 (간단하게)
tasks.test {
    useJUnitPlatform()
    
    testLogging {
        events("passed", "skipped", "failed")
    }
    
    // JavaFX 테스트를 위한 기본 설정
    jvmArgs(
        "--add-opens", "javafx.graphics/com.sun.javafx.application=ALL-UNNAMED",
        "--add-opens", "java.base/java.util.concurrent=ALL-UNNAMED"
    )
}

// 🎯 개발 실행 태스크
tasks.register("dev") {
    group = "application"
    description = "Run the desktop application in development mode with Java 21 LTS"
    dependsOn("bootRun")
}

// 🎮 배포용 태스크
tasks.register("dist") {
    group = "distribution" 
    description = "Create distribution package for Java 21 LTS desktop application"
    dependsOn("bootJar")
    
    doLast {
        println("🎮 Tetris Desktop Application (Java 21 LTS) JAR created:")
        println("   Location: ${tasks.bootJar.get().archiveFile.get().asFile}")
        println("   Run with: java -jar ${tasks.bootJar.get().archiveFile.get().asFile.name}")
    }
}
