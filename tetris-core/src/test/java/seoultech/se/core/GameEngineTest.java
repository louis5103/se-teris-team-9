package seoultech.se.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import seoultech.se.core.model.Tetromino;
import seoultech.se.core.model.enumType.RotationDirection;
import seoultech.se.core.model.enumType.TetrominoType;

/**
 * GameEngine 테스트 클래스
 * 
 * Phase 1: T-Spin Mini 구현 검증
 * - T-Spin 감지 테스트
 * - T-Spin Mini 감지 테스트
 * - kickIndex 저장 테스트
 * - 점수 계산 테스트
 */
class GameEngineTest {

    private GameState state;

    @BeforeEach
    void setUp() {
        state = new GameState(10, 20);
    }

    // ========== 기본 이동 테스트 ==========

    @Test
    @DisplayName("왼쪽 이동 - 성공")
    void testMoveLeft_Success() {
        // Given: T 블록을 중앙에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: 왼쪽으로 이동
        GameState newState = GameEngine.tryMoveLeft(state);
        
        // Then: 새로운 상태 반환, X 좌표 -1
        assertNotSame(state, newState);
        assertEquals(4, newState.getCurrentX());
        assertEquals(5, newState.getCurrentY());
    }

    @Test
    @DisplayName("왼쪽 이동 - 벽에 막힘")
    void testMoveLeft_Blocked() {
        // Given: T 블록을 왼쪽 끝에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(0);
        state.setCurrentY(5);
        
        // When: 왼쪽으로 이동 시도
        GameState newState = GameEngine.tryMoveLeft(state);
        
        // Then: 원본 상태 그대로 반환
        assertSame(state, newState);
    }

    @Test
    @DisplayName("오른쪽 이동 - 성공")
    void testMoveRight_Success() {
        // Given: T 블록을 중앙에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: 오른쪽으로 이동
        GameState newState = GameEngine.tryMoveRight(state);
        
        // Then: 새로운 상태 반환, X 좌표 +1
        assertNotSame(state, newState);
        assertEquals(6, newState.getCurrentX());
    }

    @Test
    @DisplayName("아래 이동 - 성공")
    void testMoveDown_Success() {
        // Given: T 블록을 중앙에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: 아래로 이동
        GameState newState = GameEngine.tryMoveDown(state, false);
        
        // Then: 새로운 상태 반환, Y 좌표 +1
        assertNotSame(state, newState);
        assertEquals(6, newState.getCurrentY());
    }

    // ========== 회전 테스트 ==========

    @Test
    @DisplayName("회전 - 성공 시 lastActionWasRotation 플래그 설정")
    void testRotate_SetsRotationFlag() {
        // Given: T 블록을 중앙에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: 시계방향 회전
        GameState newState = GameEngine.tryRotate(state, RotationDirection.CLOCKWISE);
        
        // Then: 회전 플래그가 true로 설정됨
        assertNotSame(state, newState);
        assertTrue(newState.isLastActionWasRotation());
    }

    @Test
    @DisplayName("회전 - kickIndex 저장 확인")
    void testRotate_SavesKickIndex() {
        // Given: T 블록을 중앙에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: 시계방향 회전
        GameState newState = GameEngine.tryRotate(state, RotationDirection.CLOCKWISE);
        
        // Then: kickIndex가 저장됨 (0-4 범위)
        assertNotSame(state, newState);
        assertTrue(newState.getLastRotationKickIndex() >= 0);
        assertTrue(newState.getLastRotationKickIndex() <= 4);
    }

    @Test
    @DisplayName("O 블록 회전 - 원본 상태 반환")
    void testRotate_OBlock_ReturnsOriginal() {
        // Given: O 블록
        Tetromino oBlock = new Tetromino(TetrominoType.O);
        state.setCurrentTetromino(oBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: 회전 시도
        GameState newState = GameEngine.tryRotate(state, RotationDirection.CLOCKWISE);
        
        // Then: 원본 상태 그대로 반환
        assertSame(state, newState);
    }

    // ========== T-Spin 테스트 ==========

    @Test
    @DisplayName("T-Spin - 3-Corner Rule 만족")
    void testTSpin_ThreeCornerRule() {
        // Given: T-Spin 상황 설정
        // T 블록 주변 3개 코너를 블록으로 채움
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(10);
        
        // 좌상, 우상, 좌하 코너를 블록으로 채움
        state.getGrid()[9][4].setOccupied(true);  // 좌상
        state.getGrid()[9][6].setOccupied(true);  // 우상
        state.getGrid()[11][4].setOccupied(true); // 좌하
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);  // 회전 플래그 설정
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin으로 감지됨
        assertTrue(locked.isLastLockWasTSpin());
    }

    @Test
    @DisplayName("T-Spin Mini - 정면 코너 1개 비어있음 (Spawn 상태)")
    void testTSpinMini_SpawnRotation() {
        // Given: T-Spin Mini 상황 설정 (Spawn = 상향)
        // 정면(위쪽) 2개 코너 중 1개만 채움
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(10);
        
        // 3-Corner Rule 만족 (3개 코너 채움)
        state.getGrid()[9][4].setOccupied(true);  // 좌상 (정면)
        state.getGrid()[11][4].setOccupied(true); // 좌하
        state.getGrid()[11][6].setOccupied(true); // 우하
        // 우상은 비어있음 (정면 코너 중 1개)
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        state.setLastRotationKickIndex(0);  // 5번째 테스트(index 4) 아님
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin Mini로 감지됨
        assertTrue(locked.isLastLockWasTSpin());
        assertTrue(locked.isLastLockWasTSpinMini());
    }

    @Test
    @DisplayName("T-Spin Mini - Wall Kick 5번째 테스트 사용 시 일반 T-Spin")
    void testTSpinMini_FifthKickTest_NotMini() {
        // Given: T-Spin 상황이지만 kickIndex가 4 (5번째 테스트)
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(10);
        
        // 3-Corner Rule 만족
        state.getGrid()[9][4].setOccupied(true);
        state.getGrid()[9][6].setOccupied(true);
        state.getGrid()[11][4].setOccupied(true);
        
        // When: 5번째 Wall Kick 테스트 사용
        state.setLastActionWasRotation(true);
        state.setLastRotationKickIndex(4);  // 5번째 테스트
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin이지만 Mini는 아님
        assertTrue(locked.isLastLockWasTSpin());
        assertFalse(locked.isLastLockWasTSpinMini());
    }

    @Test
    @DisplayName("T-Spin Mini - 정면 코너 모두 채워져 있으면 일반 T-Spin")
    void testTSpinMini_BothFrontCornersFilled_NotMini() {
        // Given: 정면 2개 코너 모두 채움
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(10);
        
        // 4개 코너 모두 채움
        state.getGrid()[9][4].setOccupied(true);  // 좌상 (정면)
        state.getGrid()[9][6].setOccupied(true);  // 우상 (정면)
        state.getGrid()[11][4].setOccupied(true); // 좌하
        state.getGrid()[11][6].setOccupied(true); // 우하
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        state.setLastRotationKickIndex(0);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin이지만 Mini는 아님
        assertTrue(locked.isLastLockWasTSpin());
        assertFalse(locked.isLastLockWasTSpinMini());
    }

    @Test
    @DisplayName("T-Spin 아님 - 회전 플래그가 false")
    void testNotTSpin_NoRotation() {
        // Given: T 블록이지만 회전하지 않음
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(10);
        
        // 3-Corner Rule 만족
        state.getGrid()[9][4].setOccupied(true);
        state.getGrid()[9][6].setOccupied(true);
        state.getGrid()[11][4].setOccupied(true);
        
        // When: 회전하지 않고 고정
        state.setLastActionWasRotation(false);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 아님
        assertFalse(locked.isLastLockWasTSpin());
        assertFalse(locked.isLastLockWasTSpinMini());
    }

    @Test
    @DisplayName("T-Spin 아님 - T 블록이 아님")
    void testNotTSpin_NotTBlock() {
        // Given: I 블록
        Tetromino iBlock = new Tetromino(TetrominoType.I);
        state.setCurrentTetromino(iBlock);
        state.setCurrentX(5);
        state.setCurrentY(10);
        
        // When: 회전 후 고정
        state.setLastActionWasRotation(true);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin 아님
        assertFalse(locked.isLastLockWasTSpin());
        assertFalse(locked.isLastLockWasTSpinMini());
    }

    // ========== 점수 계산 테스트 ==========

    @Test
    @DisplayName("T-Spin Mini 점수 - 라인 없음")
    void testTSpinMini_Score_NoLine() {
        // Given: T-Spin Mini 상황 (라인 클리어 없음)
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(18);
        
        // 바닥에 블록 배치하여 T-Spin Mini 상황 만들기
        for (int x = 0; x < 10; x++) {
            if (x != 5) {
                state.getGrid()[19][x].setOccupied(true);
            }
        }
        
        // 3-Corner Rule 만족, 정면 1개 비어있음
        state.getGrid()[17][4].setOccupied(true);
        state.getGrid()[19][4].setOccupied(true);
        state.getGrid()[19][6].setOccupied(true);
        
        // When: T-Spin Mini로 고정 (라인 클리어 없음)
        state.setLastActionWasRotation(true);
        state.setLastRotationKickIndex(0);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin Mini 점수 획득 (100 * level)
        assertTrue(locked.isLastLockWasTSpin());
        assertTrue(locked.isLastLockWasTSpinMini());
        assertEquals(GameConstants.TSPIN_MINI_NO_LINE * state.getLevel(), locked.getLastScoreEarned());
    }

    @Test
    @DisplayName("T-Spin Mini 점수 - Single (1줄)")
    void testTSpinMini_Score_Single() {
        // Given: T-Spin Mini 상황 (1줄 클리어)
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(17);
        
        // 바닥 1줄을 거의 채움 (T 블록 들어갈 공간만 남김)
        for (int x = 0; x < 10; x++) {
            if (x != 5) {
                state.getGrid()[19][x].setOccupied(true);
            }
        }
        
        // 3-Corner Rule 만족, 정면 1개 비어있음
        state.getGrid()[16][4].setOccupied(true);
        state.getGrid()[18][4].setOccupied(true);
        state.getGrid()[18][6].setOccupied(true);
        
        // When: T-Spin Mini로 고정 (1줄 클리어)
        state.setLastActionWasRotation(true);
        state.setLastRotationKickIndex(0);
        GameState locked = GameEngine.lockTetromino(state);
        
        // Then: T-Spin Mini Single 점수 획득 (200 * level)
        assertTrue(locked.isLastLockWasTSpin());
        assertTrue(locked.isLastLockWasTSpinMini());
        assertEquals(1, locked.getLastLinesCleared());
        assertEquals(GameConstants.TSPIN_MINI_SINGLE * state.getLevel(), locked.getLastScoreEarned());
    }

    // ========== Hard Drop 테스트 ==========

    @Test
    @DisplayName("Hard Drop - 즉시 바닥까지 이동 및 고정")
    void testHardDrop() {
        // Given: T 블록을 위쪽에 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: Hard Drop
        GameState dropped = GameEngine.hardDrop(state);
        
        // Then: 바닥에 고정됨, 거리만큼 점수 획득
        assertNotSame(state, dropped);
        // Hard Drop으로 인한 점수 확인 (거리 * 2)
        assertTrue(dropped.getScore() > 0);
    }

    // ========== Hold 테스트 ==========

    @Test
    @DisplayName("Hold - 첫 Hold 성공")
    void testHold_FirstTime() {
        // Given: T 블록, nextQueue에 다음 블록 준비
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        state.setNextQueue(new TetrominoType[]{TetrominoType.I, TetrominoType.O});
        
        // When: Hold
        GameState held = GameEngine.tryHold(state);
        
        // Then: T가 Hold되고, I가 현재 블록이 됨
        assertNotSame(state, held);
        assertEquals(TetrominoType.T, held.getHeldPiece());
        assertEquals(TetrominoType.I, held.getCurrentTetromino().getType());
        assertTrue(held.isHoldUsedThisTurn());
    }

    @Test
    @DisplayName("Hold - 이미 사용한 턴에 재사용 불가")
    void testHold_AlreadyUsed() {
        // Given: 이미 Hold를 사용한 상태
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        state.setHoldUsedThisTurn(true);
        
        // When: Hold 재시도
        GameState held = GameEngine.tryHold(state);
        
        // Then: 원본 상태 그대로 반환
        assertSame(state, held);
    }

    // ========== GameState 불변성 테스트 ==========

    @Test
    @DisplayName("실패한 이동은 원본 상태를 반환")
    void testImmutability_FailedMove() {
        // Given: 왼쪽 끝에 블록 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(0);
        state.setCurrentY(5);
        
        // When: 왼쪽 이동 시도
        GameState result = GameEngine.tryMoveLeft(state);
        
        // Then: 같은 객체 반환 (deepCopy 하지 않음)
        assertSame(state, result);
    }

    @Test
    @DisplayName("성공한 이동은 새로운 상태를 반환")
    void testImmutability_SuccessfulMove() {
        // Given: 중앙에 블록 배치
        Tetromino tBlock = new Tetromino(TetrominoType.T);
        state.setCurrentTetromino(tBlock);
        state.setCurrentX(5);
        state.setCurrentY(5);
        
        // When: 왼쪽 이동
        GameState result = GameEngine.tryMoveLeft(state);
        
        // Then: 다른 객체 반환 (deepCopy 수행)
        assertNotSame(state, result);
        // 원본은 변경되지 않음
        assertEquals(5, state.getCurrentX());
        // 새 상태는 변경됨
        assertEquals(4, result.getCurrentX());
    }
}
