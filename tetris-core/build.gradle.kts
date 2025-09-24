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
    // 📊 유틸리티 (기본적인 기능만)
    implementation("org.apache.commons:commons-lang3:3.17.0")
    
    // 🧪 테스트 전용 의존성 (기본적인 것들만)
    testImplementation("org.assertj:assertj-core:3.26.3")
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
    testLogging {
        events("passed", "skipped", "failed")
    }
}
