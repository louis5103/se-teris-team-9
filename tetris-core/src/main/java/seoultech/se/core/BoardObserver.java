package seoultech.se.core;

import seoultech.se.core.model.block.Tetromino;
import seoultech.se.core.model.block.enumType.RotationDirection;
import seoultech.se.core.model.block.enumType.TetrominoType;
import seoultech.se.core.model.board.Cell;
import seoultech.se.core.model.board.GameState;

/**
 * Board 상태 변경을 관찰하는 인터페이스 (SRS 테트리스 완전 사양)
 *
 * 이 인터페이스는 Observer 패턴의 핵심입니다. Board에서 일어나는 모든 중요한 이벤트를
 * 외부 시스템(UI, 사운드, 네트워크 등)에 알리기 위해 설계되었습니다.
 *
 * 작동 원리:
 * 1. Board는 이 인터페이스를 구현한 Observer들의 목록을 가지고 있습니다
 * 2. Board의 상태가 변경되면, 등록된 모든 Observer의 해당 메서드를 호출합니다
 * 3. Observer(예: GameController)는 이 알림을 받아서 필요한 작업을 수행합니다
 *
 * 구현 가이드:
 * - 모든 메서드를 구현할 필요는 없습니다
 * - 당장 필요하지 않은 메서드는 빈 구현으로 남겨두면 됩니다
 * - 나중에 기능을 추가할 때 해당 메서드만 채우면 됩니다
 *
 * 예시:
 * public class GameController implements BoardObserver {
 *     // 필요한 메서드만 실제 로직 구현
 *     public void onCellChanged(int row, int col, Cell cell) {
 *         updateUI(row, col, cell);
 *     }
 *
 *     // 나머지는 빈 구현
 *     public void onCombo(int comboCount) {
 *         // TODO: 나중에 구현
 *     }
 * }
 */
public interface BoardObserver {

    // ========================================================================
    // 기본 셀/보드 변경 이벤트
    // ========================================================================

    /**
     * 특정 셀이 변경되었을 때 호출됩니다.
     *
     * 호출 시점:
     * - 테트로미노가 고정되어 셀이 색상을 가지고 점유될 때
     * - 라인이 클리어되어 셀이 비워질 때
     * - 라인이 아래로 당겨져서 셀의 내용이 변경될 때
     *
     * 구현 예시:
     * - JavaFX UI: 해당 위치의 Rectangle 색상 변경
     * - 네트워크: 다른 플레이어에게 변경사항 전송
     *
     * @param row 변경된 셀의 행 위치 (0부터 시작, 위에서 아래로)
     * @param col 변경된 셀의 열 위치 (0부터 시작, 왼쪽에서 오른쪽으로)
     * @param cell 변경된 셀 객체 (색상, 점유 상태 포함)
     */
    void onCellChanged(int row, int col, Cell cell);

    /**
     * 여러 셀이 한꺼번에 변경되었을 때 호출됩니다.
     *
     * 이것은 성능 최적화를 위한 메서드입니다. 라인 클리어처럼 많은 셀이 동시에 변경될 때,
     * onCellChanged를 수십 번 호출하는 대신 이 메서드를 한 번 호출하면 더 효율적입니다.
     *
     * 호출 시점:
     * - 라인 클리어로 전체 라인이 아래로 당겨질 때
     * - 쓰레기 라인이 추가될 때 (멀티플레이어)
     *
     * 구현 팁:
     * - 당장은 구현하지 않아도 됩니다
     * - onCellChanged를 여러 번 호출하는 것으로 충분합니다
     * - 나중에 성능이 문제가 되면 그때 구현하세요
     *
     * @param rows 변경된 셀들의 행 위치 배열
     * @param cols 변경된 셀들의 열 위치 배열
     * @param cells 변경된 셀 객체들의 2차원 배열
     */
    void onMultipleCellsChanged(int[] rows, int[] cols, Cell[][] cells);

    // ========================================================================
    // 테트로미노 이동/회전 이벤트
    // ========================================================================

    /**
     * 현재 테트로미노가 이동했을 때 호출됩니다.
     *
     * 이 메서드는 테트리스 게임에서 가장 자주 호출되는 메서드입니다.
     * 플레이어가 키를 누를 때마다, 그리고 자동으로 낙하할 때마다 호출됩니다.
     *
     * 호출 시점:
     * - moveLeft(), moveRight(), moveDown()이 성공했을 때
     * - 회전 후 위치가 변경되었을 때
     * - Hard Drop으로 순간 이동했을 때
     *
     * 구현 시 주의사항:
     * - 이 메서드는 매우 자주 호출되므로 가벼운 작업만 수행해야 합니다
     * - 복잡한 계산이나 애니메이션은 별도 스레드에서 처리하세요
     *
     * @param x 새로운 X 좌표 (보드 중앙 기준)
     * @param y 새로운 Y 좌표 (위에서 아래로)
     * @param tetromino 현재 테트로미노 객체
     */
    void onTetrominoMoved(int x, int y, Tetromino tetromino);

    /**
     * 테트로미노가 회전했을 때 호출됩니다.
     *
     * SRS에서 회전은 단순히 블록을 돌리는 것이 아닙니다. Wall Kick 시스템이 적용되어,
     * 벽이나 다른 블록에 막혔을 때 자동으로 위치를 조정합니다. kickIndex 파라미터로
     * 어떤 Wall Kick이 적용되었는지 알 수 있습니다.
     *
     * 호출 시점:
     * - rotateClockwise() 또는 rotateCounterClockwise()가 성공했을 때
     *
     * kickIndex의 의미:
     * - 0: 회전만 성공 (위치 조정 없음)
     * - 1~4: Wall Kick 테이블의 특정 offset이 적용됨
     *
     * 구현 예시:
     * - 회전 사운드 재생 (kickIndex에 따라 다른 사운드)
     * - 회전 이펙트 표시
     * - T-Spin 판정을 위한 정보 저장
     *
     * @param direction 회전 방향 (CLOCKWISE 또는 COUNTER_CLOCKWISE)
     * @param kickIndex 적용된 Wall Kick 인덱스 (0~4)
     */
    void onTetrominoRotated(RotationDirection direction, int kickIndex);

    /**
     * 테트로미노 회전이 실패했을 때 호출됩니다.
     *
     * 모든 Wall Kick 시도가 실패하여 회전할 수 없을 때 호출됩니다.
     * 플레이어에게 "회전이 불가능하다"는 피드백을 주기 위해 사용합니다.
     *
     * 호출 시점:
     * - 회전하려 했지만 벽, 바닥, 다른 블록에 막혀서 불가능할 때
     *
     * 구현 예시:
     * - 실패 사운드 재생
     * - 화면 살짝 흔들기
     * - 회전 버튼 깜빡이기
     *
     * @param direction 시도한 회전 방향
     */
    void onTetrominoRotationFailed(RotationDirection direction);

    /**
     * 테트로미노가 보드에 고정되었을 때 호출됩니다.
     *
     * 테트로미노가 더 이상 내려갈 수 없어서 보드에 영구적으로 고정될 때 호출됩니다.
     * 이 시점부터 해당 블록은 움직일 수 없고, 라인 클리어 판정이 시작됩니다.
     *
     * 호출 시점:
     * - moveDown()에서 더 이상 내려갈 수 없을 때
     * - Hard Drop으로 즉시 고정될 때
     * - Lock Delay 타이머가 만료되었을 때
     *
     * 구현 예시:
     * - 고정 사운드 재생 ("쿵" 소리)
     * - 고정 이펙트 (블록이 빛나거나 진동)
     * - Hold 가능 상태 리셋
     *
     * @param tetromino 고정된 테트로미노
     */
    void onTetrominoLocked(Tetromino tetromino);

    /**
     * Lock Delay가 시작되었을 때 호출됩니다.
     *
     * Lock Delay는 SRS의 중요한 기능으로, 블록이 바닥에 닿았을 때 즉시 고정되지 않고
     * 약간의 유예 시간을 줍니다. 이 시간 동안 플레이어는 블록을 이동하거나 회전시켜서
     * 더 나은 위치를 찾을 수 있습니다.
     *
     * 호출 시점:
     * - 테트로미노가 바닥이나 다른 블록 위에 처음 착지했을 때
     *
     * 구현 예시:
     * - Lock Delay 타이머 UI 표시
     * - 블록 색상을 약간 어둡게 변경
     * - 경고음 재생
     */
    void onTetrominoLockDelayStarted();

    /**
     * Lock Delay가 리셋되었을 때 호출됩니다.
     *
     * 바닥에 닿은 상태에서 블록을 이동하거나 회전시키면 Lock Delay가 리셋됩니다.
     * 하지만 무한정 리셋할 수는 없고, 보통 15회 정도로 제한됩니다.
     *
     * 호출 시점:
     * - Lock Delay 중에 블록을 이동 또는 회전시켰을 때
     *
     * @param remainingResets 남은 리셋 횟수 (0이 되면 강제 고정)
     */
    void onTetrominoLockDelayReset(int remainingResets);

    // ========================================================================
    // 테트로미노 생성 이벤트
    // ========================================================================

    /**
     * 새로운 테트로미노가 생성되었을 때 호출됩니다.
     *
     * 게임 시작 시와 블록이 고정된 후 다음 블록이 나올 때마다 호출됩니다.
     *
     * 호출 시점:
     * - 게임 시작 시 첫 블록 생성
     * - 블록이 고정된 후 다음 블록 생성
     * - Hold 사용 시 새 블록으로 교체
     *
     * 구현 예시:
     * - 생성 사운드 재생
     * - 블록 등장 애니메이션
     * - Hold 가능 상태 활성화
     *
     * @param tetromino 생성된 테트로미노
     */
    void onTetrominoSpawned(Tetromino tetromino);

    /**
     * Next 큐가 업데이트되었을 때 호출됩니다.
     *
     * 7-bag 시스템에서 다음에 나올 블록들의 순서가 결정되거나 변경될 때 호출됩니다.
     * 보통 게임 시작 시와 가방이 비어서 새로 채워질 때 호출됩니다.
     *
     * 호출 시점:
     * - 게임 시작 시 초기 큐 생성
     * - 7개 블록을 모두 사용해서 새 가방을 만들 때
     *
     * 배열 내용:
     * - nextPieces[0]: 다음에 나올 블록
     * - nextPieces[1]: 그 다음 블록
     * - ...
     * - 보통 5~6개 정도를 미리 보여줍니다
     *
     * 구현 예시:
     * - Next 블록 UI 업데이트
     * - 각 블록을 작은 미리보기로 표시
     *
     * @param nextPieces 다음에 나올 블록들의 배열
     */
    void onNextQueueUpdated(TetrominoType[] nextPieces);

    // ========================================================================
    // Hold 시스템 이벤트
    // ========================================================================

    /**
     * Hold가 실행되어 블록이 교체되었을 때 호출됩니다.
     *
     * Hold는 현재 블록을 저장하고 Hold 슬롯에 있던 블록을 꺼내는 기능입니다.
     * Hold 슬롯이 비어있었다면, 현재 블록을 저장하고 다음 블록을 꺼냅니다.
     * 한 턴에 한 번만 사용할 수 있으며, 블록이 고정되면 다시 사용 가능해집니다.
     *
     * 호출 시점:
     * - 플레이어가 Hold 키를 눌렀을 때 (첫 Hold 또는 교체)
     *
     * 파라미터 조합:
     * - heldPiece != null, previousPiece == null: 처음 Hold 사용
     * - heldPiece != null, previousPiece != null: Hold 슬롯 블록과 교체
     *
     * 구현 예시:
     * - Hold UI 업데이트 (현재 저장된 블록 표시)
     * - Hold 사운드 재생
     * - Hold 불가능 상태로 변경 (이번 턴에는 더 이상 Hold 불가)
     *
     * @param heldPiece 현재 Hold 슬롯에 있는 블록
     * @param previousPiece 이전에 Hold 슬롯에 있던 블록 (null이면 처음 Hold)
     */
    void onHoldChanged(TetrominoType heldPiece, TetrominoType previousPiece);

    /**
     * Hold를 시도했지만 실패했을 때 호출됩니다.
     *
     * 한 턴에 한 번만 Hold를 사용할 수 있으므로, 이미 사용했다면 다시 시도 시 실패합니다.
     *
     * 호출 시점:
     * - 이번 턴에 이미 Hold를 사용한 상태에서 다시 Hold 시도
     *
     * 구현 예시:
     * - 실패 사운드 재생 ("삐" 소리)
     * - Hold UI 깜빡이기
     * - "Already used Hold" 메시지 표시
     */
    void onHoldFailed();

    // ========================================================================
    // 라인 클리어 이벤트
    // ========================================================================

    /**
     * 라인이 클리어되었을 때 호출됩니다.
     *
     * 이것은 테트리스 게임의 가장 중요한 이벤트입니다. 한 줄 이상이 완성되어
     * 사라질 때 호출되며, 다양한 추가 정보(T-Spin, Perfect Clear 등)를 함께 제공합니다.
     *
     * 호출 시점:
     * - 블록이 고정된 후 라인 클리어 체크를 통과했을 때
     *
     * 라인 수별 명칭:
     * - 1줄: Single
     * - 2줄: Double
     * - 3줄: Triple
     * - 4줄: Tetris (가장 높은 점수)
     *
     * T-Spin 종류:
     * - isTSpin=true, isTSpinMini=false: 일반 T-Spin (높은 점수)
     * - isTSpin=true, isTSpinMini=true: T-Spin Mini (보통 점수)
     * - isTSpin=false: 일반 클리어
     *
     * Perfect Clear:
     * - 보드의 모든 블록이 사라졌을 때 true
     * - 매우 높은 보너스 점수
     *
     * 구현 예시:
     * - 클리어 애니메이션 (줄이 빛나면서 사라짐)
     * - 클리어 타입에 따른 다른 사운드
     * - "TETRIS!", "T-SPIN DOUBLE!" 같은 텍스트 표시
     * - Perfect Clear 시 특수 이펙트
     *
     * @param linesCleared 클리어된 라인 수 (1~4)
     * @param clearedRows 클리어된 행 번호들의 배열
     * @param isTSpin T-Spin 여부
     * @param isTSpinMini T-Spin Mini 여부
     * @param isPerfectClear Perfect Clear (All Clear) 여부
     */
    void onLineCleared(int linesCleared, int[] clearedRows,
                      boolean isTSpin, boolean isTSpinMini, boolean isPerfectClear);

    /**
     * 콤보가 발생했을 때 호출됩니다.
     *
     * 콤보는 연속으로 라인을 지울 때 증가하는 카운터입니다. 라인을 지우지 못하면
     * 콤보가 끊깁니다. 콤보가 높을수록 추가 점수를 받습니다.
     *
     * 호출 시점:
     * - 라인 클리어 후, 이전에도 라인을 지운 적이 있을 때
     *
     * 콤보 카운트:
     * - 1: 첫 번째 연속 클리어
     * - 2: 두 번째 연속 클리어
     * - ...
     * - 높은 콤보는 지수적으로 점수가 증가
     *
     * 구현 예시:
     * - "COMBO x3" 텍스트 표시
     * - 콤보 수에 따라 색상 변경 (x3: 노란색, x5: 빨간색, x10: 무지개)
     * - 콤보 사운드 재생 (콤보가 높을수록 높은 음)
     *
     * @param comboCount 현재 콤보 수 (1부터 시작)
     */
    void onCombo(int comboCount);

    /**
     * 콤보가 끊겼을 때 호출됩니다.
     *
     * 라인을 지우지 못하고 블록을 놓았을 때 콤보가 리셋됩니다.
     *
     * 호출 시점:
     * - 블록을 고정했지만 라인을 지우지 못했을 때
     *
     * 구현 예시:
     * - 콤보 UI 페이드 아웃
     * - 콤보 종료 사운드
     * - 최종 콤보 수가 높았다면 축하 메시지
     *
     * @param finalComboCount 끊기기 직전의 최종 콤보 수
     */
    void onComboBreak(int finalComboCount);

    /**
     * Back-to-Back이 발생했을 때 호출됩니다.
     *
     * Back-to-Back(B2B)는 "어려운 클리어"를 연속으로 할 때 발생합니다.
     * 어려운 클리어란 Tetris(4줄) 또는 T-Spin을 의미합니다.
     * 일반 클리어(Single, Double, Triple)를 하면 B2B가 끊깁니다.
     *
     * 호출 시점:
     * - Tetris 또는 T-Spin 후, 이전에도 Tetris 또는 T-Spin을 한 적이 있을 때
     *
     * 점수 보너스:
     * - B2B가 활성화되어 있으면 점수가 1.5배
     * - B2B가 누적되면 보너스가 더 커질 수 있음
     *
     * 구현 예시:
     * - "BACK-TO-BACK x2" 텍스트 표시
     * - 화면 가장자리가 빛나는 이펙트
     * - B2B 전용 사운드 재생
     *
     * @param backToBackCount 현재 B2B 카운트
     */
    void onBackToBack(int backToBackCount);

    /**
     * Back-to-Back이 끊겼을 때 호출됩니다.
     *
     * 일반 클리어(Single, Double, Triple)를 하거나 라인을 지우지 못하면 B2B가 끊깁니다.
     *
     * 호출 시점:
     * - B2B 상태에서 일반 클리어를 했을 때
     * - 라인을 지우지 못하고 블록을 놓았을 때
     *
     * @param finalBackToBackCount 끊기기 직전의 최종 B2B 카운트
     */
    void onBackToBackBreak(int finalBackToBackCount);

    // ========================================================================
    // 점수 및 게임 상태 이벤트
    // ========================================================================

    /**
     * 점수가 추가되었을 때 호출됩니다.
     *
     * 라인 클리어, 소프트 드롭, 하드 드롭, 콤보, B2B 등 모든 점수 획득 시 호출됩니다.
     * reason 파라미터로 어떤 행동으로 점수를 얻었는지 알 수 있습니다.
     *
     * 호출 시점:
     * - 라인을 지웠을 때
     * - 소프트 드롭으로 내려갈 때 (1칸당 1점)
     * - 하드 드롭으로 떨어뜨릴 때 (1칸당 2점)
     * - 콤보/B2B 보너스 점수
     *
     * reason 예시:
     * - "SINGLE": 1줄 클리어
     * - "TETRIS": 4줄 클리어
     * - "T-SPIN_DOUBLE": T-Spin으로 2줄 클리어
     * - "PERFECT_CLEAR": Perfect Clear 보너스
     * - "SOFT_DROP": 소프트 드롭 점수
     * - "HARD_DROP": 하드 드롭 점수
     * - "COMBO_5": 콤보 5 보너스
     *
     * 구현 예시:
     * - 획득 점수를 화면에 표시 ("+800 TETRIS")
     * - 점수 획득 위치에 이펙트 표시
     * - 높은 점수 획득 시 특수 사운드
     *
     * @param points 추가된 점수
     * @param reason 점수 획득 이유를 나타내는 문자열
     */
    void onScoreAdded(long points, String reason);

    /**
     * GameState가 변경되었을 때 호출됩니다.
     *
     * 점수, 레벨, 라인 수 등 게임의 전반적인 상태가 변경될 때 호출됩니다.
     * 여러 속성이 동시에 변경될 수 있으므로, 이 메서드에서 전체 UI를 갱신하는 것이 효율적입니다.
     *
     * 호출 시점:
     * - 점수가 변경되었을 때
     * - 레벨이 올랐을 때
     * - 라인 수가 증가했을 때
     *
     * 구현 예시:
     * - 점수 레이블 업데이트
     * - 레벨 레이블 업데이트
     * - 라인 수 레이블 업데이트
     * - 레벨에 따라 낙하 속도 조정
     * - 레벨에 따라 배경색 변경
     *
     * @param gameState 변경된 게임 상태 객체
     */
    void onGameStateChanged(GameState gameState);

    /**
     * 레벨이 올랐을 때 호출됩니다.
     *
     * 보통 10줄을 지울 때마다 레벨이 1씩 올라갑니다.
     * 레벨이 올라가면 블록의 낙하 속도가 빨라집니다.
     *
     * 호출 시점:
     * - 누적 라인 수가 10의 배수에 도달했을 때
     *
     * 구현 예시:
     * - "LEVEL UP!" 텍스트와 함께 화면 깜빡임
     * - 레벨업 사운드 재생
     * - 배경 음악 템포 증가
     * - 배경 그래픽 변경
     *
     * @param newLevel 새로운 레벨 (1, 2, 3, ...)
     */
    void onLevelUp(int newLevel);

    // ========================================================================
    // 게임 진행 이벤트
    // ========================================================================

    /**
     * 게임이 일시정지되었을 때 호출됩니다.
     *
     * 호출 시점:
     * - 플레이어가 일시정지 키를 눌렀을 때
     * - 게임이 백그라운드로 이동했을 때
     *
     * 구현 예시:
     * - 일시정지 오버레이 표시
     * - 게임 루프 정지
     * - 배경 음악 일시정지
     */
    void onGamePaused();

    /**
     * 일시정지가 해제되어 게임이 재개되었을 때 호출됩니다.
     *
     * 호출 시점:
     * - 일시정지 상태에서 재개 키를 눌렀을 때
     *
     * 구현 예시:
     * - 일시정지 오버레이 제거
     * - 게임 루프 재시작
     * - 배경 음악 재개
     * - "3...2...1..." 카운트다운 표시
     */
    void onGameResumed();

    /**
     * 게임이 종료되었을 때 호출됩니다.
     *
     * 테트리스에서 게임 오버가 되는 조건은 여러 가지입니다. reason 파라미터로
     * 어떤 이유로 게임이 끝났는지 알 수 있습니다.
     *
     * 호출 시점:
     * - 새 블록이 spawn 위치에 놓일 수 없을 때
     *
     * reason 종류:
     * - "BLOCK_OUT": 새 블록이 spawn 위치를 벗어남 (보드 위쪽)
     * - "LOCK_OUT": 블록이 spawn 위치에서 고정됨
     * - "TOP_OUT": 쌓인 블록이 spawn 위치까지 올라옴
     *
     * 구현 예시:
     * - 게임 오버 화면 표시
     * - 최종 점수 표시
     * - 통계 정보 표시 (총 라인, 최고 콤보 등)
     * - 게임 오버 사운드 재생
     * - 리더보드 업데이트
     * - 재시작/메인메뉴 버튼 표시
     *
     * @param reason 게임 종료 이유를 나타내는 문자열
     */
    void onGameOver(String reason);

    // ========================================================================
    // 멀티플레이어 이벤트
    // ========================================================================

    /**
     * 쓰레기 라인이 추가되었을 때 호출됩니다.
     *
     * 멀티플레이어에서 다른 플레이어가 공격했을 때 보드 아래쪽에 쓰레기 라인이 추가됩니다.
     * 쓰레기 라인은 무작위 위치에 구멍이 하나 있는 완전한 줄입니다.
     *
     * 호출 시점:
     * - 다른 플레이어가 라인을 지워서 공격을 보냈을 때
     * - 게임 모드에 따라 일정 시간마다 자동으로 추가될 때
     *
     * 구현 예시:
     * - 경고 표시 ("2 lines incoming!")
     * - 쓰레기 라인이 올라오는 애니메이션
     * - 공격 받음 사운드
     * - 공격자 이름 표시
     *
     * @param lines 추가된 쓰레기 라인 수
     * @param sourcePlayerId 공격한 플레이어의 ID (null이면 시스템 공격)
     */
    void onGarbageLinesAdded(int lines, String sourcePlayerId);

    /**
     * 쓰레기 라인이 제거되었을 때 호출됩니다.
     *
     * 받을 예정이던 쓰레기 라인을 라인 클리어로 상쇄했을 때 호출됩니다.
     * 예를 들어 4줄 공격이 들어올 예정인데 2줄을 지우면, 2줄이 상쇄되고 2줄만 받습니다.
     *
     * 호출 시점:
     * - 라인 클리어로 들어올 쓰레기를 상쇄했을 때
     *
     * 구현 예시:
     * - 방어 성공 이펙트 (방패 아이콘)
     * - 상쇄된 라인 수 표시
     * - 방어 사운드 재생
     *
     * @param lines 상쇄된 쓰레기 라인 수
     */
    void onGarbageLinesCleared(int lines);

    /**
     * 다른 플레이어를 공격했을 때 호출됩니다.
     *
     * 라인을 지워서 다른 플레이어에게 쓰레기 라인을 보냈을 때 호출됩니다.
     *
     * 호출 시점:
     * - 라인 클리어 후 공격 계산이 완료되었을 때
     *
     * 공격 라인 수:
     * - Single: 0줄 (공격 없음)
     * - Double: 1줄
     * - Triple: 2줄
     * - Tetris: 4줄
     * - T-Spin Single: 2줄
     * - T-Spin Double: 4줄
     * - T-Spin Triple: 6줄
     * - 콤보/B2B 보너스 추가
     *
     * 구현 예시:
     * - 공격 이펙트 (화살표가 날아가는 애니메이션)
     * - "4 lines sent!" 텍스트 표시
     * - 공격 사운드 재생
     * - 대상 플레이어의 보드에 경고 표시
     *
     * @param targetPlayerId 공격 대상 플레이어의 ID
     * @param lines 보낸 쓰레기 라인 수
     */
    void onAttackSent(String targetPlayerId, int lines);

    // ========================================================================
    // 디버그/개발 이벤트
    // ========================================================================

    /**
     * 디버그 정보가 업데이트되었을 때 호출됩니다.
     *
     * 개발 중에 게임 상태를 추적하기 위한 메서드입니다.
     * 프로덕션 빌드에서는 무시해도 됩니다.
     *
     * 호출 시점:
     * - 디버그 모드에서 중요한 상태 변경이 있을 때
     *
     * debugInfo 예시:
     * - "Current FPS: 60"
     * - "Lock Delay: 0.3s remaining"
     * - "Next bag: I,J,L,O,S,T,Z"
     * - "T-Spin corners filled: 3/4"
     *
     * @param debugInfo 디버그 정보 문자열
     */
    void onDebugInfoUpdated(String debugInfo);
}
