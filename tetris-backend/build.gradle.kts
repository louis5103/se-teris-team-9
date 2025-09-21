/*
 * 'tetris-backend' 모듈의 빌드 설정입니다.
 * Spring Boot를 이용한 비즈니스 로직을 담당합니다.
 */
plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    // ✅ 핵심: 'tetris-core' 모듈을 의존성으로 추가하여 핵심 로직을 사용합니다.
    implementation(project(":tetris-core"))

    // Spring Boot 및 데이터베이스 관련 의존성을 추가합니다.
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 개발용 인메모리 DB
}
