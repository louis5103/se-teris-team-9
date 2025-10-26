# EventMapper 리팩토링 계획

## 🔴 긴급 수정사항

### 1. LockResult에 고정된 블록 정보 추가

**수정할 파일**: `tetris-core/src/main/java/seoultech/se/core/result/LockResult.java`

```java
@Value
public class LockResult {
    boolean gameOver;
    GameState newState;
    LineClearResult lineClearResult;
    String gameOverReason;
    
    // 추가: 고정된 블록 정보
    Tetromino lockedTetromino;
    int lockedX;
    int lockedY;

    public static LockResult success(
        GameState newState, 
        LineClearResult lineClearResult,
        Tetromino lockedTetromino,
        int lockedX,
        int lockedY
    ) {
        return new LockResult(
            false, 
            newState, 
            lineClearResult, 
            null,
            lockedTetromino,
            lockedX,
            lockedY
        );
    }

    public static LockResult gameOver(
        GameState newState, 
        String reason,
        Tetromino lockedTetromino,
        int lockedX,
        int lockedY
    ) {
        return new LockResult(
            true, 
            newState, 
            LineClearResult.none(), 
            reason,
            lockedTetromino,
            lockedX,
            lockedY
        );
    }
}
```

### 2. GameEngine.lockTetromino() 수정

**수정할 파일**: `tetris-core/src/main/java/seoultech/se/core/GameEngine.java`

```java
public static LockResult lockTetromino(GameState state) {
    GameState newState = state.deepCopy();
    
    // 고정하기 전에 블록 정보 저장!
    Tetromino lockedTetromino = state.getCurrentTetromino();
    int lockedX = state.getCurrentX();
    int lockedY = state.getCurrentY();

    // 1. Grid에 테트로미노 고정
    int[][] shape = state.getCurrentTetromino().getCurrentShape();

    for(int row = 0; row < shape.length; row++) {
        for(int col = 0; col < shape[row].length; col++) {
            if (shape[row][col] == 1) {
                int absX = state.getCurrentX() + (col - state.getCurrentTetromino().getPivotX());
                int absY = state.getCurrentY() + (row - state.getCurrentTetromino().getPivotY());

                // 게임 오버 체크
                if( absY < 0 ) {
                    newState.setGameOver(true);
                    return LockResult.gameOver(
                        newState, 
                        "[GameEngine] (Method: lockTetromino) Game Over: Block locked above the board.",
                        lockedTetromino,
                        lockedX,
                        lockedY
                    );
                }
                
                if(absY >= 0 && absY < state.getBoardHeight() &&
                   absX >= 0 && absX < state.getBoardWidth()
                ) {
                    newState.getGrid()[absY][absX].setColor(state.getCurrentTetromino().getColor());
                    newState.getGrid()[absY][absX].setOccupied(true);
                }
            }
        }
    }

    // 2. 라인 클리어 체크 및 실행
    LineClearResult clearResult = checkAndClearLines(newState);

    // 3. 점수 및 통계 업데이트
    // ... (기존 코드)

    // 4. Hold 재사용 가능하게 설정
    newState.setHoldUsedThisTurn(false);
    
    return LockResult.success(
        newState, 
        clearResult,
        lockedTetromino,  // 고정된 블록 정보 전달!
        lockedX,
        lockedY
    );
}
```

### 3. EventMapper.fromLockResult() 수정

**수정할 파일**: `tetris-client/src/main/java/seoultech/se/client/mapper/EventMapper.java`

```java
public static List<GameEvent> fromLockResult(
        LockResult result,
        GameState gameState,
        long gameStartTime
) {
    List<GameEvent> events = new ArrayList<>();

    // 1. 블록 고정 Event - 이제 올바른 블록 정보 사용!
    events.add(new TetrominoLockedEvent(
        result.getLockedTetromino(),  // ✅ 수정됨
        result.getLockedX(),           // ✅ 수정됨
        result.getLockedY()            // ✅ 수정됨
    ));

    // 2. 게임 오버 체크
    if (result.isGameOver()) {
        events.addAll(createGameOverEvents(result, gameState, gameStartTime));
        return events;
    }

    // ... (나머지 코드 동일)

    return events;
}
```

## 🟡 추가 개선사항

### 4. 오래된 주석 업데이트

**Line 102-103**: 주석이 코드와 불일치

```java
// 수정 전
// 5. 새 블록 관련 이벤트는 BoardController에서 추가
// (spawnNewTetromino() 호출 후 이벤트 생성)

// 수정 후
// 5. 새 블록 관련 이벤트는 BoardController에서 별도 생성
// createTetrominoSpawnEvents() 메서드 사용
```

### 5. getScoreReason() 메서드 접근성 검토

현재 `public static`인데, 외부에서 사용되지 않는다면 `private static`으로 변경 고려:

```java
// BoardController나 다른 곳에서 사용하는지 확인 필요
// 사용하지 않는다면:
private static String getScoreReason(LineClearResult result) {
    // ...
}
```

### 6. GameStateChangedEvent 중복 체크

현재 여러 곳에서 GameStateChangedEvent가 발생할 수 있습니다:
- Line 108: 정상 흐름
- Line 148: 게임 오버

이것이 의도된 것인지 확인하고, 불필요한 중복이라면 제거 고려.

### 7. 테스트 코드 작성 추천

EventMapper는 순수 함수이므로 테스트하기 좋습니다:

```java
@Test
void testFromLockResult_shouldUseLockedTetrominoInfo() {
    // Given: 고정된 블록 정보가 있는 LockResult
    Tetromino lockedBlock = new Tetromino(TetrominoType.T);
    LockResult result = LockResult.success(
        newState, 
        LineClearResult.none(),
        lockedBlock,
        5,
        10
    );
    
    // When: EventMapper로 변환
    List<GameEvent> events = EventMapper.fromLockResult(result, gameState, startTime);
    
    // Then: TetrominoLockedEvent가 올바른 블록 정보를 포함
    TetrominoLockedEvent lockedEvent = (TetrominoLockedEvent) events.get(0);
    assertEquals(TetrominoType.T, lockedEvent.getTetromino().getType());
    assertEquals(5, lockedEvent.getX());
    assertEquals(10, lockedEvent.getY());
}
```

## 🔵 추가 고려사항

### 8. hardDrop() 메서드도 수정 필요

GameEngine.hardDrop()도 lockTetromino()를 호출하므로 자동으로 수정됩니다.

### 9. 향후 확장성

멀티플레이어를 고려한다면:
- LockResult를 JSON으로 직렬화 가능하게 만들기
- Event도 직렬화 가능하게 만들기

## 📊 우선순위

1. **긴급 (1-3)**: 버그 수정 - 즉시 적용
2. **중요 (4-6)**: 코드 품질 개선 - 다음 스프린트
3. **선택 (7-9)**: 테스트 및 확장성 - 여유 있을 때

## 🎯 예상 작업 시간

- LockResult 수정: 30분
- GameEngine 수정: 30분
- EventMapper 수정: 15분
- 테스트 및 검증: 1시간
- **총 예상 시간: 2시간 15분**
