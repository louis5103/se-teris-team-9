/*
 * Tetris Core Module
 * 🎯 순수 Java 알고리즘 및 도메인 로직
 * - Tetris 게임 핵심 알고리즘
 * - 블록, 보드, 점수 등 도메인 모델
 * - 외부 의존성 없는 POJO 구현
 */
plugins {
    `java-library`  // 다른 모듈에서 라이브러리로 사용
}

description = "Tetris Core Domain Logic"

dependencies {
    // 📊 Utility Libraries
    implementation(libs.common.commons.lang3)
    
    // �️ Development Tools (공통 의존성)
    compileOnly(libs.common.lombok)
    annotationProcessor(libs.common.lombok)
    testCompileOnly(libs.common.lombok)
    testAnnotationProcessor(libs.common.lombok)
    
    // 🧪 Testing Dependencies (공통 번들)
    testImplementation(libs.bundles.common.testing)
}

// 📦 JAR 생성 설정
tasks.jar {
    archiveBaseName.set("tetris-core")
    manifest {
        attributes(
            "Implementation-Title" to "Tetris Core",
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "SeoulTech SE Team 9"
        )
    }
}

// ✅ 테스트 실행 설정
tasks.test {
    useJUnitPlatform()
    
    // 테스트 로깅 개선
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    
    // 테스트 실행 최적화
    maxParallelForks = 1
}
