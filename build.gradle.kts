/*
 * 이 파일은 모든 하위 모듈에 공통으로 적용될 설정을 정의합니다.
 */
plugins {
    java
    // Spring Boot 플러그인을 버전만 정의하고, 실제 적용은 필요한 모듈에서 합니다.
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

// 모든 하위 프로젝트(subprojects)에 아래 설정을 공통으로 적용합니다.
subprojects {
    // GroupID를 'seoultech.se'로 통일합니다.
    group = "seoultech.se"
    version = "1.0.0-SNAPSHOT"

    apply(plugin = "java")

    // Java 버전을 11로 설정합니다.
    java.sourceCompatibility = JavaVersion.VERSION_11
    java.targetCompatibility = JavaVersion.VERSION_11

    repositories {
        mavenCentral()
    }

    dependencies {
        // JUnit5를 모든 모듈의 테스트 기본 의존성으로 추가합니다.
        testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
