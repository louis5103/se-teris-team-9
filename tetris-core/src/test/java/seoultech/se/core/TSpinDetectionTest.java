package seoultech.se.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;
import seoultech.se.core.model.enumType.RotationState;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * T-Spin 감지 정확성 테스트
 * 
 * 목적: T-Spin 및 T-Spin Mini 감지 로직의 정확성 검증
 * 
 * 테스트 시나리오:
 * 1. 회전 상태별 T-Spin 감지 (SPAWN, RIGHT, REVERSE, LEFT)
 * 2. T-Spin vs T-Spin Mini 구분
 * 3. 3-Corner Rule 검증
 * 4. 회전 없이 배치한 경우 T-Spin 미감지
 * 5. Edge Case: 벽 근처, 보드 밖 코너
 */
class TSpinDetectionTest {

    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState(10, 20);
    }

    // ========== T-Spin 기본 감지 테스트 ==========

    @Test
    @DisplayName("T-Spin - SPAWN 상태에서 회전 후 감지")
    void testTSpin_SpawnState() {
        // Given: T 블록을 SPAWN 상태로 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        assertEquals(RotationState.SPAWN, tBlock.getRotationState());
        
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // 3개 코너 채우기 (pivot: 5, 18)
        state.getGrid()[17][4].setOccupied(true);  // 좌상 (4,17)
        state.getGrid()[17][6].setOccupied(true);  // 우상 (6,17)
        state.getGrid()[19][4].setOccupied(true);  // 좌하 (4,19)
        // state.getGrid()[19][6] - 우하 (6,19) 비워둠
        
        // When: 회전했다고 플래그 설정 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지
        assertTrue(locked.isLastLockWasTSpin(), "T-Spin이 감지되어야 합니다");
        assertFalse(locked.isLastLockWasTSpinMini(), "T-Spin Mini가 아닙니다");
    }

    @Test
    @DisplayName("T-Spin - RIGHT 상태에서 회전 후 감지")
    void testTSpin_RightState() {
        // Given: T 블록을 RIGHT 상태로 회전
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        tBlock = tBlock.getRotatedInstance(RotationDirection.CLOCKWISE);
        assertEquals(RotationState.RIGHT, tBlock.getRotationState());
        
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // RIGHT 상태의 pivot 기준 코너 채우기
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[17][6].setOccupied(true);
        state.getGrid()[19][4].setOccupied(true);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지
        assertTrue(locked.isLastLockWasTSpin());
    }

    @Test
    @DisplayName("T-Spin - REVERSE 상태에서 회전 후 감지")
    void testTSpin_ReverseState() {
        // Given: T 블록을 REVERSE 상태로 회전
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        tBlock = tBlock.getRotatedInstance(RotationDirection.CLOCKWISE);
        tBlock = tBlock.getRotatedInstance(RotationDirection.CLOCKWISE);
        assertEquals(RotationState.REVERSE, tBlock.getRotationState());
        
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // REVERSE 상태의 코너 채우기
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[17][6].setOccupied(true);
        state.getGrid()[19][6].setOccupied(true);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지
        assertTrue(locked.isLastLockWasTSpin());
    }

    @Test
    @DisplayName("T-Spin - LEFT 상태에서 회전 후 감지")
    void testTSpin_LeftState() {
        // Given: T 블록을 LEFT 상태로 회전
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        tBlock = tBlock.getRotatedInstance(RotationDirection.COUNTER_CLOCKWISE);
        assertEquals(RotationState.LEFT, tBlock.getRotationState());
        
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // LEFT 상태의 코너 채우기
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[17][6].setOccupied(true);
        state.getGrid()[19][6].setOccupied(true);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지
        assertTrue(locked.isLastLockWasTSpin());
    }

    // ========== T-Spin vs T-Spin Mini 구분 테스트 ==========

    @Test
    @DisplayName("T-Spin Mini - kickIndex 0이 아니고 정면 코너 비어있음")
    void testTSpinMini_KickIndex_NonZero() {
        // Given: T 블록 SPAWN 상태
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // 3개 코너 채우되 정면 1개 비워둠
        state.getGrid()[17][4].setOccupied(true);  // 좌상
        // state.getGrid()[17][6] - 우상 비움 (정면)
        state.getGrid()[19][4].setOccupied(true);  // 좌하
        state.getGrid()[19][6].setOccupied(true);  // 우하
        
        // When: kickIndex != 0으로 회전 후 고정
        state.setLastActionWasRotation(true);
        state.setLastRotationKickIndex(2);  // kickIndex 0이 아님
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin Mini 감지
        assertTrue(locked.isLastLockWasTSpin());
        assertTrue(locked.isLastLockWasTSpinMini(), "kickIndex != 0이면 T-Spin Mini");
    }

    @Test
    @DisplayName("T-Spin (일반) - kickIndex 0이고 정면 코너 채워짐")
    void testTSpin_Regular_KickIndexZero_FrontFilled() {
        // Given: T 블록 SPAWN 상태
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // 3개 코너 채우되 정면 포함
        state.getGrid()[17][4].setOccupied(true);  // 좌상
        state.getGrid()[17][6].setOccupied(true);  // 우상 (정면)
        state.getGrid()[19][4].setOccupied(true);  // 좌하
        // state.getGrid()[19][6] - 우하 비움
        
        // When: kickIndex 0으로 회전 후 고정
        state.setLastActionWasRotation(true);
        state.setLastRotationKickIndex(0);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin (일반) 감지
        assertTrue(locked.isLastLockWasTSpin());
        assertFalse(locked.isLastLockWasTSpinMini(), "정면 코너가 채워지면 T-Spin (일반)");
    }

    // ========== 3-Corner Rule 테스트 ==========

    @Test
    @DisplayName("3-Corner Rule - 정확히 3개 코너 채워짐")
    void testThreeCornerRule_ExactlyThree() {
        // Given: T 블록
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // 정확히 3개 코너만 채움
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[17][6].setOccupied(true);
        state.getGrid()[19][4].setOccupied(true);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지
        assertTrue(locked.isLastLockWasTSpin());
    }

    @Test
    @DisplayName("3-Corner Rule - 4개 모두 채워짐")
    void testThreeCornerRule_AllFour() {
        // Given: T 블록
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // 4개 코너 모두 채움
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[17][6].setOccupied(true);
        state.getGrid()[19][4].setOccupied(true);
        state.getGrid()[19][6].setOccupied(true);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지 (4개도 3개 이상)
        assertTrue(locked.isLastLockWasTSpin());
    }

    @Test
    @DisplayName("3-Corner Rule 실패 - 2개만 채워짐")
    void testThreeCornerRule_OnlyTwo() {
        // Given: T 블록
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // 2개 코너만 채움
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[17][6].setOccupied(true);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 미감지 (3개 미만)
        assertFalse(locked.isLastLockWasTSpin(), "2개 코너는 T-Spin이 아님");
    }

    // ========== 회전 플래그 테스트 ==========

    @Test
    @DisplayName("회전 없이 배치 - T-Spin 미감지")
    void testNoRotation_NoTSpin() {
        // Given: T 블록, 3개 코너 채워짐
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[17][6].setOccupied(true);
        state.getGrid()[19][4].setOccupied(true);
        
        // When: 회전 플래그 없이 고정
        state.setLastActionWasRotation(false);  // 중요!
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 미감지
        assertFalse(locked.isLastLockWasTSpin(), "회전하지 않으면 T-Spin 아님");
    }

    @Test
    @DisplayName("이동 후 회전 플래그 리셋 확인")
    void testRotationFlag_ResetAfterMove() {
        // Given: T 블록 회전 후 이동
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // 회전
        GameState rotated = GameEngine.tryRotate(state, RotationDirection.CLOCKWISE);
        assertTrue(rotated.isLastActionWasRotation());
        
        // When: 이동
        GameState moved = GameEngine.tryMoveLeft(rotated);
        
        // Then: 회전 플래그 리셋
        assertFalse(moved.isLastActionWasRotation(), "이동 시 회전 플래그 리셋");
    }

    // ========== Edge Case 테스트 ==========

    @Test
    @DisplayName("Edge Case - 벽 근처에서 T-Spin")
    void testTSpin_NearWall() {
        // Given: T 블록을 왼쪽 벽에 바로 인접하게 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(0);  // 왼쪽 벽 (x=0)
        state.setCurrentY(18);
        
        // pivot (0, 18), 코너: (-1,17), (1,17), (-1,19), (1,19)
        // x=-1은 벽 밖이므로 (-1,17), (-1,19)는 "채워진 것"으로 간주 = 2개
        // 추가로 1개 더 채워야 3개 이상 = T-Spin
        state.getGrid()[17][1].setOccupied(true);  // 우상 (1,17) - 3번째
        // 우하 (1,19)는 비워둠
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지 (벽 밖 2개 + 블록 1개 = 3개 코너)
        assertTrue(locked.isLastLockWasTSpin());
    }

    @Test
    @DisplayName("Edge Case - 보드 바닥에서 T-Spin")
    void testTSpin_AtBottom() {
        // Given: T 블록을 바닥 근처에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(19);  // 바닥 (y=19)
        
        // pivot (5, 19), 코너: (4,18), (6,18), (4,20), (6,20)
        // (4,20), (6,20)는 보드 밖 - "채워진 것"으로 간주
        state.getGrid()[18][4].setOccupied(true);
        state.getGrid()[18][6].setOccupied(true);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 감지 (보드 밖도 코너로 인정)
        assertTrue(locked.isLastLockWasTSpin());
    }

    // ========== Non T-block 테스트 ==========

    @Test
    @DisplayName("I 블록 - T-Spin 미감지")
    void testNonTBlock_NoTSpin() {
        // Given: I 블록
        Tetromino iBlock = new Tetromino(TetrominoType.I);
        state.setCurrentTetromino(iBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 미감지 (T 블록이 아님)
        assertFalse(locked.isLastLockWasTSpin());
    }
}
