package seoultech.se.core.command;

public enum CommandType {
    // ========== 기본 이동 ==========
    MOVE_LEFT,
    MOVE_RIGHT,
    MOVE_DOWN,

    SOFT_DROP,
    HARD_DROP,

    // ========== 유저 기능 ==========
    HOLD,
    SONIC_DROP,


    // ========== 회전 ==========
    ROTATE_CLOCKWISE,
    ROTATE_COUNTER_CLOCKWISE,

    // ========== 시스템 게임 제어 ==========
    PAUSE,
    RESUME,
    RESTART,

    // ========== 서버 전용 커맨드 (클라이언트는 직접 호출 불가) ==========
    SPAWN_TETROMINO,     // (서버가 7-bag에서 선택)
    ADD_GARBAGE_LINES,   // 쓰레기 라인 추가 (멀티플레이어 공격)
    CLEAR_GARBAGE_LINES, // 쓰레기 라인 제거 (방어)

    // ========== 디버그/치트 커맨드 (개발용) ==========
    DEBUG_SPAWN_SPECIFIC_TETROMINO,  // 특정 블록 강제 생성
    DEBUG_CLEAR_BOARD,               // 보드 전체 초기화
    DEBUG_FILL_LINES                 // 테스트용 라인 채우기

    // TICK
    //
}
