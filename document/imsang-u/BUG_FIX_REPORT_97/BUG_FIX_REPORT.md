# 테트리스 게임 버그 수정 리포트

> **작성일**: 2025-01-14  
> **프로젝트**: Tetris Multi-Module Game  
> **수정 범위**: 치명적/중대 버그 6건

---

## 목차

1. [버그 #1: Hold 기능에서 Next Queue 동기화 문제](#버그-1-hold-기능에서-next-queue-동기화-문제)
2. [버그 #2: Hold 사용 후 스폰 위치 충돌 검사 누락](#버그-2-hold-사용-후-스폰-위치-충돌-검사-누락)
3. [버그 #3: 콤보 카운트 로직 개선](#버그-3-콤보-카운트-로직-개선)
4. [버그 #4: Hard Drop의 불필요한 deepCopy](#버그-4-hard-drop의-불필요한-deepcopy)
5. [버그 #5: O 블록 회전 시 불변성 위반](#버그-5-o-블록-회전-시-불변성-위반)
6. [버그 #6: 게임 오버 후 블록 고정 계속 진행](#버그-6-게임-오버-후-블록-고정-계속-진행)

---

## 버그 #1: Hold 기능에서 Next Queue 동기화 문제

### 🔴 심각도: 치명적 (Critical)

### 📋 버그 설명

Hold 기능 사용 시 GameEngine과 BoardController의 블록 생성 시스템이 동기화되지 않아 Next Queue에 표시되는 블록과 실제로 나오는 블록이 다를 수 있는 문제.

### 🐛 원인 분석

- **GameEngine**: Hold 사용 시 `updateNextQueue()`에서 **단순 랜덤**으로 새 블록 추가
- **BoardController**: 일반 블록 생성 시 **7-bag 시스템** 사용
- 두 시스템이 완전히 분리되어 동기화되지 않음

### 📝 재현 시나리오

1. Hold가 비어있는 상태에서 Hold 사용
2. GameEngine이 랜덤으로 Next Queue 업데이트
3. BoardController는 7-bag 시스템으로 실제 블록 생성
4. **UI에 표시된 Next 블록과 실제 블록 불일치**

### 💻 기존 코드

**GameEngine.java - tryHold() 메서드**:
```java
if (previousHeld == null) {
    // Hold가 비어있음
    newState.setHeldPiece(currentType);
    
    TetrominoType nextType = newState.getNextQueue()[0];
    Tetromino newTetromino = new Tetromino(nextType);
    
    newState.setCurrentTetromino(newTetromino);
    newState.setCurrentX(newState.getBoardWidth() / 2 - 1);
    newState.setCurrentY(0);
    
    // ❌ 문제: 단순 랜덤으로 큐 업데이트
    updateNextQueue(newState);
}

private static void updateNextQueue(GameState state) {
    TetrominoType[] queue = state.getNextQueue();
    TetrominoType[] newQueue = new TetrominoType[queue.length];
    
    System.arraycopy(queue, 1, newQueue, 0, queue.length - 1);
    
    // ❌ 단순 랜덤 - 7-bag과 충돌
    TetrominoType[] allTypes = TetrominoType.values();
    newQueue[queue.length - 1] = allTypes[(int)(Math.random() * allTypes.length)];
    
    state.setNextQueue(newQueue);
}
```

### ✅ 수정된 코드

**GameEngine.java**:
```java
if (previousHeld == null) {
    newState.setHeldPiece(currentType);
    
    TetrominoType nextType = newState.getNextQueue()[0];
    Tetromino newTetromino = new Tetromino(nextType);
    
    int spawnX = newState.getBoardWidth() / 2 - 1;
    int spawnY = 0;
    
    newState.setCurrentTetromino(newTetromino);
    newState.setCurrentX(spawnX);
    newState.setCurrentY(spawnY);
    
    // ✅ Next Queue 업데이트는 BoardController에서 처리
    // (7-bag 시스템과 동기화)
}

// ✅ updateNextQueue() 메서드 완전 제거
```

**BoardController.java - handleHoldCommand() 메서드**:
```java
private List<GameEvent> handleHoldCommand() {
    List<GameEvent> events = new ArrayList<>();
    
    seoultech.se.core.result.HoldResult result = GameEngine.tryHold(gameState);
    
    if (result.isSuccess()) {
        gameState = result.getNewState();
        
        // ✅ Hold가 비어있었던 경우, 7-bag 시스템으로 업데이트
        if (result.getPreviousHeldPiece() == null) {
            updateNextQueue();  // BoardController의 7-bag 시스템 사용
        }
        
        // ... 이벤트 발생
    }
    
    return events;
}
```

### 📊 수정 효과

- ✅ GameEngine의 랜덤 로직 제거
- ✅ BoardController가 7-bag 시스템으로 일관되게 관리
- ✅ Next Queue와 실제 블록 생성 완벽하게 동기화

---

## 버그 #2: Hold 사용 후 스폰 위치 충돌 검사 누락

### 🔴 심각도: 치명적 (Critical)

### 📋 버그 설명

Hold로 블록을 교체할 때 스폰 위치에 이미 블록이 있는지 검사하지 않아, 블록이 겹쳐지거나 게임 오버 조건을 무시하는 문제.

### 🐛 원인 분석

Hold에서 블록을 꺼낼 때:
- 스폰 위치 (x, y) 설정만 함
- **충돌 검사 없음**
- 스폰 위치에 블록이 있어도 그대로 배치
- 게임 오버 조건 무시

### 📝 재현 시나리오

1. 보드를 상단까지 쌓음 (스폰 위치가 막힘)
2. Hold를 사용하여 블록 교체
3. 새 블록이 기존 블록과 겹쳐서 배치됨
4. **게임이 계속 진행되거나 이상한 상태 발생**

### 💻 기존 코드

**GameEngine.java - tryHold() 메서드**:
```java
if (previousHeld == null) {
    // Hold가 비어있음
    newState.setHeldPiece(currentType);
    TetrominoType nextType = newState.getNextQueue()[0];
    Tetromino newTetromino = new Tetromino(nextType);
    
    // ❌ 충돌 검사 없이 그냥 배치
    newState.setCurrentTetromino(newTetromino);
    newState.setCurrentX(newState.getBoardWidth() / 2 - 1);
    newState.setCurrentY(0);
} else {
    // Hold에 블록이 있음
    newState.setHeldPiece(currentType);
    Tetromino heldTetromino = new Tetromino(previousHeld);
    
    // ❌ 여기도 충돌 검사 없음
    newState.setCurrentTetromino(heldTetromino);
    newState.setCurrentX(newState.getBoardWidth() / 2 - 1);
    newState.setCurrentY(0);
}
```

### ✅ 수정된 코드

**GameEngine.java - tryHold() 메서드**:
```java
if (previousHeld == null) {
    newState.setHeldPiece(currentType);
    
    TetrominoType nextType = newState.getNextQueue()[0];
    Tetromino newTetromino = new Tetromino(nextType);
    
    int spawnX = newState.getBoardWidth() / 2 - 1;
    int spawnY = 0;
    
    // ✅ 스폰 위치 충돌 검사 추가
    if (!isValidPosition(newState, newTetromino, spawnX, spawnY)) {
        newState.setGameOver(true);
        newState.setGameOverReason("Cannot spawn new tetromino after hold: spawn position blocked");
        return HoldResult.failure("Game Over: Cannot spawn new tetromino");
    }
    
    // 스폰 성공
    newState.setCurrentTetromino(newTetromino);
    newState.setCurrentX(spawnX);
    newState.setCurrentY(spawnY);
    
} else {
    newState.setHeldPiece(currentType);
    
    Tetromino heldTetromino = new Tetromino(previousHeld);
    int spawnX = newState.getBoardWidth() / 2 - 1;
    int spawnY = 0;
    
    // ✅ 스폰 위치 충돌 검사 추가
    if (!isValidPosition(newState, heldTetromino, spawnX, spawnY)) {
        newState.setGameOver(true);
        newState.setGameOverReason("Cannot swap held tetromino: spawn position blocked");
        return HoldResult.failure("Game Over: Cannot swap held tetromino");
    }
    
    // 스폰 성공
    newState.setCurrentTetromino(heldTetromino);
    newState.setCurrentX(spawnX);
    newState.setCurrentY(spawnY);
}
```

**BoardController.java - handleHoldCommand() 메서드**:
```java
private List<GameEvent> handleHoldCommand() {
    List<GameEvent> events = new ArrayList<>();
    
    seoultech.se.core.result.HoldResult result = GameEngine.tryHold(gameState);
    
    if (result.isSuccess()) {
        // ... 성공 처리
    } else {
        // ✅ Hold 실패 처리 개선
        if (gameState.isGameOver()) {
            // 게임 오버 Event 발생
            events.add(new GameOverEvent(result.getFailureReason()));
        } else {
            // 일반 Hold 실패
            events.add(new HoldFailedEvent(result.getFailureReason()));
        }
    }
    
    return events;
}
```

### 📊 수정 효과

- ✅ 스폰 위치 충돌 검사 추가
- ✅ 충돌 시 게임 오버 처리
- ✅ 블록 겹침 현상 방지
- ✅ 정확한 게임 종료 조건 보장

---

## 버그 #3: 콤보 카운트 로직 개선

### 🟡 심각도: 경미 (Minor) - 실제로는 정상 동작, 가독성 개선

### 📋 버그 설명

콤보 카운트 로직이 정상 동작하지만, 주석이 부족하여 의도가 명확하지 않음.

### 🐛 원인 분석

- 로직 자체는 정상: `comboCount + 1`
- 주석 부족으로 동작 이해 어려움
- B2B 로직도 주석 미비

### 💻 기존 코드

**GameEngine.java - lockTetromino() 메서드**:
```java
// 3. 점수 및 통계 업데이트
if(clearResult.getLinesCleared() > 0) {
    newState.addScore(clearResult.getScoreEarned());
    newState.addLinesCleared(clearResult.getLinesCleared());

    // 콤보 업데이트
    newState.setComboCount(newState.getComboCount() + 1);
    newState.setLastActionClearedLines(true);

    // B2B 업데이트 
    boolean isDifficult = clearResult.getLinesCleared() == 4 || clearResult.isTSpin();
    if(isDifficult && newState.isLastClearWasDifficult()) {
        newState.setBackToBackCount(newState.getBackToBackCount() + 1);
    } else if (isDifficult) {
        newState.setBackToBackCount(1);
    } else {
        newState.setBackToBackCount(0);
    }
    newState.setLastClearWasDifficult(isDifficult);;
} else { // 라인 클리어 못했으면 콤보 초기화
    newState.setComboCount(0);
    newState.setLastActionClearedLines(false);
    newState.setBackToBackCount(0);
    newState.setLastClearWasDifficult(false);
}
```

### ✅ 수정된 코드

**GameEngine.java - lockTetromino() 메서드**:
```java
// 4. 점수 및 통계 업데이트
if(clearResult.getLinesCleared() > 0) {
    newState.addScore(clearResult.getScoreEarned());
    newState.addLinesCleared(clearResult.getLinesCleared());

    // ✅ 콤보 업데이트 (연속 라인 클리어 횟수)
    // 0 → 1 (첫 콤보), 1 → 2 (콤보 계속), 2 → 3, ...
    newState.setComboCount(newState.getComboCount() + 1);
    newState.setLastActionClearedLines(true);

    // ✅ B2B (Back-to-Back) 업데이트
    // Tetris(4줄) 또는 T-Spin을 연속으로 성공하면 B2B 카운트 증가
    boolean isDifficult = clearResult.getLinesCleared() == 4 || clearResult.isTSpin();
    if(isDifficult && newState.isLastClearWasDifficult()) {
        // 이전에도 difficult였고 지금도 difficult → B2B 계속
        newState.setBackToBackCount(newState.getBackToBackCount() + 1);
    } else if (isDifficult) {
        // 처음으로 difficult 클리어 → B2B 시작
        newState.setBackToBackCount(1);
    } else {
        // 일반 클리어 (1~3줄) → B2B 종료
        newState.setBackToBackCount(0);
    }
    newState.setLastClearWasDifficult(isDifficult);
} else { 
    // ✅ 라인 클리어 실패 → 모든 연속 보너스 초기화
    newState.setComboCount(0);
    newState.setLastActionClearedLines(false);
    newState.setBackToBackCount(0);
    newState.setLastClearWasDifficult(false);
}
```

### 📊 수정 효과

- ✅ 상세한 주석으로 로직 의도 명확화
- ✅ 콤보 동작 과정 설명: "0 → 1 → 2 → ..."
- ✅ B2B 각 분기별 설명 추가
- ✅ 코드 가독성 향상

---

## 버그 #4: Hard Drop의 불필요한 deepCopy

### 🟠 심각도: 중대 (Major) - 성능 문제

### 📋 버그 설명

Hard Drop 실행 시 `hardDrop()`과 `lockTetromino()`에서 각각 deepCopy가 발생하여 불필요한 성능 저하.

### 🐛 원인 분석

- `hardDrop()`에서 deepCopy #1 수행
- 내부적으로 `lockTetromino()` 호출
- `lockTetromino()`에서 deepCopy #2 수행
- **총 2번의 deepCopy** (20x10 그리드 전체 복사)

### 📝 성능 영향

- deepCopy는 매우 비싼 연산 (모든 Cell 객체 복사)
- Hard Drop은 매우 자주 사용되는 기능
- **불필요한 50% 성능 오버헤드**

### 💻 기존 코드

**GameEngine.java**:
```java
public static LockResult hardDrop(GameState state){
    // ❌ deepCopy #1
    GameState droppedState = state.deepCopy();
    int dropDistance = 0;

    while(isValidPosition(droppedState, droppedState.getCurrentTetromino(), 
                          droppedState.getCurrentX(), droppedState.getCurrentY() + 1)) {
        droppedState.setCurrentY(droppedState.getCurrentY() + 1);
        dropDistance++;
    }

    droppedState.addScore(dropDistance * 2);

    // ❌ lockTetromino에서 deepCopy #2 발생
    return lockTetromino(droppedState);
}

public static LockResult lockTetromino(GameState state) {
    // ❌ deepCopy #2
    GameState newState = state.deepCopy();
    // ...
}
```

### ✅ 수정된 코드

**GameEngine.java**:
```java
public static LockResult hardDrop(GameState state){
    // ✅ 1. 원본 state는 수정하지 않고 거리만 계산
    int dropDistance = 0;
    int finalY = state.getCurrentY();

    while(isValidPosition(state, state.getCurrentTetromino(), 
                          state.getCurrentX(), finalY + 1)) {
        finalY++;
        dropDistance++;
    }

    // ✅ 2. deepCopy를 한 번만 수행
    GameState droppedState = state.deepCopy();
    droppedState.setCurrentY(finalY);
    droppedState.addScore(dropDistance * 2);

    // ✅ 3. 이미 복사되었으므로 내부에서 다시 복사하지 않음
    return lockTetrominoInternal(droppedState, false);
}

public static LockResult lockTetromino(GameState state) {
    return lockTetrominoInternal(state, true);
}

// ✅ 내부 메서드: 선택적 deepCopy
private static LockResult lockTetrominoInternal(GameState state, boolean needsCopy) {
    GameState newState = needsCopy ? state.deepCopy() : state;
    // ... 기존 로직
}
```

### 📊 수정 효과

**Before**:
```
hardDrop()
  ├─ deepCopy #1
  └─ lockTetromino()
      └─ deepCopy #2 ← 불필요!
```

**After**:
```
hardDrop()
  ├─ 거리 계산만
  ├─ deepCopy #1
  └─ lockTetrominoInternal(state, false)
      └─ deepCopy 건너뜀 ✓
```

- ✅ deepCopy 횟수: 2번 → 1번 (50% 감소)
- ✅ Hard Drop 성능 개선
- ✅ 메모리 사용량 감소

---

## 버그 #5: O 블록 회전 시 불변성 위반

### 🟠 심각도: 중대 (Major) - 설계 원칙 위반

### 📋 버그 설명

O 블록 회전 시 원본 GameState를 그대로 반환하여 불변성 원칙을 위반.

### 🐛 원인 분석

- 다른 모든 메서드는 **새로운 GameState 반환** (불변성 유지)
- O 블록만 **원본 state 그대로 반환**
- 불변성 원칙 위반
- Command 패턴과의 불일치

### 📝 잠재적 문제

1. **상태 공유 문제**:
   ```java
   GameState original = ...;
   RotationResult result = GameEngine.tryRotate(original, CLOCKWISE);
   GameState rotated = result.getNewState();
   
   // O 블록: rotated === original (같은 객체!)
   // rotated 수정 시 original도 함께 수정됨
   ```

2. **Command undo/redo 구현 시 문제**
3. **멀티스레드 환경에서 동시성 문제**

### 💻 기존 코드

**GameEngine.java - tryRotate() 메서드**:
```java
public static RotationResult tryRotate(GameState state, RotationDirection direction) {
    // ❌ 원본 state를 그대로 반환
    if(state.getCurrentTetromino().getType() == TetrominoType.O) {
        return RotationResult.success(state, direction, 0);
    }
    
    // 다른 블록들은 새로운 state 반환
    // ...
}
```

### ✅ 수정된 코드

**GameEngine.java - tryRotate() 메서드**:
```java
public static RotationResult tryRotate(GameState state, RotationDirection direction) {
    // ✅ O 블록도 새로운 state 반환 (불변성 유지)
    if(state.getCurrentTetromino().getType() == TetrominoType.O) {
        return RotationResult.success(state.deepCopy(), direction, 0);
    }
    
    // 다른 블록들과 일관된 동작
    // ...
}
```

### 📊 수정 효과

- ✅ 불변성 원칙 준수
- ✅ 모든 메서드가 일관된 동작
- ✅ Command 패턴과 호환
- ✅ 상태 공유 문제 방지
- ✅ 예측 가능한 동작

**성능 고려**:
- O 블록은 7개 중 1개 (14.3%)
- 회전은 이동보다 덜 빈번
- **일관성의 이점 > 미세한 성능 차이**

---

## 버그 #6: 게임 오버 후 블록 고정 계속 진행

### 🔴 심각도: 치명적 (Critical)

### 📋 버그 설명

블록 고정 중 게임 오버를 감지하면, 일부 블록만 고정된 불완전한 상태로 게임이 종료되는 문제.

### 🐛 원인 분석

- 블록을 하나씩 순회하며 고정
- 중간에 게임 오버 감지 시 즉시 return
- **이미 고정된 일부 블록은 보드에 남음**
- 보드 상태가 일관되지 않음

### 📝 재현 시나리오

```
T 테트로미노 (4개 블록):
┌─────────┐
│    □    │ y = -1 (보드 위)
│   □□□   │ y = 0
└─────────┘

기존 로직:
1. 첫 번째 블록 체크 및 고정 시도
2. y = -1 감지 → 게임오버 return
3. 나머지 3개 블록은 처리 안 됨
4. 불완전한 보드 상태
```

### 💻 기존 코드

**GameEngine.java - lockTetromino() 메서드**:
```java
public static LockResult lockTetromino(GameState state) {
    GameState newState = state.deepCopy();
    
    // ❌ 블록을 고정하면서 동시에 게임 오버 체크
    int[][] shape = state.getCurrentTetromino().getCurrentShape();
    
    for(int row = 0; row < shape.length; row++) {
        for(int col = 0; col < shape[row].length; col++) {
            if (shape[row][col] == 1) {
                int absX = state.getCurrentX() + (col - ...);
                int absY = state.getCurrentY() + (row - ...);

                // ❌ 게임 오버 체크를 고정 중에 수행
                if(absY < 0) {
                    newState.setGameOver(true);
                    return LockResult.gameOver(...);
                    // 이미 일부 블록은 고정됨!
                }
                
                // 블록 고정
                newState.getGrid()[absY][absX].setColor(...);
                newState.getGrid()[absY][absX].setOccupied(true);
            }
        }
    }
}
```

### ✅ 수정된 코드

**GameEngine.java - lockTetrominoInternal() 메서드**:
```java
private static LockResult lockTetrominoInternal(GameState state, boolean needsCopy) {
    GameState newState = needsCopy ? state.deepCopy() : state;
    
    Tetromino lockedTetromino = state.getCurrentTetromino();
    int lockedX = state.getCurrentX();
    int lockedY = state.getCurrentY();
    int[][] shape = state.getCurrentTetromino().getCurrentShape();

    // ✅ 1. 게임 오버 체크를 먼저 수행 (블록 고정 전)
    for(int row = 0; row < shape.length; row++) {
        for(int col = 0; col < shape[row].length; col++) {
            if (shape[row][col] == 1) {
                int absY = state.getCurrentY() + (row - state.getCurrentTetromino().getPivotY());
                
                if(absY < 0) {
                    // 게임 오버 - 즉시 반환 (블록 고정 안 함)
                    newState.setGameOver(true);
                    return LockResult.gameOver(
                        newState, 
                        "[GameEngine] Game Over: Block locked above the board.",
                        lockedTetromino, lockedX, lockedY
                    );
                }
            }
        }
    }

    // ✅ 2. 블록 고정 (게임 오버가 아닌 경우에만 실행)
    for(int row = 0; row < shape.length; row++) {
        for(int col = 0; col < shape[row].length; col++) {
            if (shape[row][col] == 1) {
                int absX = state.getCurrentX() + (col - state.getCurrentTetromino().getPivotX());
                int absY = state.getCurrentY() + (row - state.getCurrentTetromino().getPivotY());

                // 블록 고정 (이미 게임 오버 체크 통과)
                if(absY >= 0 && absY < state.getBoardHeight() &&
                   absX >= 0 && absX < state.getBoardWidth()) {
                    newState.getGrid()[absY][absX].setColor(state.getCurrentTetromino().getColor());
                    newState.getGrid()[absY][absX].setOccupied(true);
                }
            }
        }
    }
    
    // 3. 라인 클리어 체크 및 실행
    // 4. 점수 계산
    // ...
}
```

### 📊 수정 효과

**Before**:
```
T 테트로미노:
1. 블록 체크 → 고정 → 게임오버 감지 → return
2. 일부 블록만 고정된 상태
3. 보드 상태 불일치
```

**After**:
```
T 테트로미노:
1. 모든 블록 위치 먼저 확인
2. 게임오버 조건 발견 → 즉시 return
3. 블록 고정 단계에 도달하지 않음
4. 보드 상태 깨끗하게 유지
```

- ✅ 게임 오버 조건 명확
- ✅ 보드 상태 일관성 보장
- ✅ 예측 가능한 동작
- ✅ 렌더링 오류 방지

---

## 📊 전체 수정 요약

| 번호 | 버그 | 심각도 | 영향 | 상태 |
|------|------|--------|------|------|
| 1 | Hold-NextQueue 동기화 | 🔴 치명적 | UI 불일치 | ✅ 완료 |
| 2 | Hold 스폰 충돌 미검사 | 🔴 치명적 | 게임 크래시 | ✅ 완료 |
| 3 | 콤보 카운트 로직 | 🟡 경미 | 가독성 | ✅ 완료 |
| 4 | 불필요한 deepCopy | 🟠 중대 | 성능 저하 | ✅ 완료 |
| 5 | O블록 불변성 위반 | 🟠 중대 | 설계 원칙 | ✅ 완료 |
| 6 | 게임오버 불완전 고정 | 🔴 치명적 | 상태 불일치 | ✅ 완료 |

### 주요 개선 사항

1. **시스템 동기화**: 7-bag 시스템 일관성 확보
2. **충돌 검사**: 모든 스폰 시점에 충돌 검사 추가
3. **성능 최적화**: 불필요한 deepCopy 제거
4. **설계 원칙**: 불변성 원칙 준수
5. **상태 일관성**: 게임 오버 처리 개선

### 테스트 권장 사항

- ✅ Hold 기능 반복 테스트
- ✅ 보드 상단까지 블록 쌓고 Hold 사용
- ✅ Hard Drop 성능 측정
- ✅ O 블록 회전 반복
- ✅ 게임 오버 조건 테스트

---

## 📝 참고 사항

### 수정된 파일 목록

1. `tetris-core/src/main/java/seoultech/se/core/GameEngine.java`
2. `tetris-client/src/main/java/seoultech/se/client/controller/BoardController.java`

### 추가 고려사항

- 7-10번 경미한 버그 수정 검토 필요
- T-Spin 감지 로직 구현 필요
- Soft Drop 점수 추가 고려
- 레벨업 로직 개선 검토

---

**문서 작성**: 2025-01-14  
**최종 업데이트**: 2025-01-14
