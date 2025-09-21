/*
 * 이 파일은 프로젝트에 어떤 하위 모듈들이 포함되는지 정의합니다.
 */
plugins {
    // JDK를 자동으로 다운로드해주는 플러그인입니다.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

// 루트 프로젝트의 이름을 'tetris-app'으로 설정합니다.
rootProject.name = "tetris-app"

// 3개의 하위 모듈을 프로젝트에 포함시킵니다.
include("tetris-core")
include("tetris-backend")
include("tetris-client")
