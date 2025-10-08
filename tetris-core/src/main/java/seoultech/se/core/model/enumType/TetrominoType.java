package seoultech.se.core.model.enumType;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.MODULE)
public enum TetrominoType {
    I(new int[][]{
            {0, 0, 0, 0},
            {1, 1, 1, 1},
            {0, 0, 0, 0},
            {0, 0, 0, 0}
    }, Color.CYAN, 1, 2),

    J(new int[][]{
            {0, 1, 0},
            {0, 1, 0},
            {1, 1, 0}
    }, Color.BLUE, 1, 1),

    L(new int[][]{
            {0, 1, 0},
            {0, 1, 0},
            {0, 1, 1}
    }, Color.ORANGE, 1, 1),

    O(new int[][]{
            {1, 1},
            {1, 1}
    }, Color.YELLOW, 0, 0),

    S(new int[][]{
            {0, 0, 0},
            {0, 1, 1},
            {1, 1, 0}
    }, Color.GREEN, 1, 1),

    T(new int[][]{
            {0, 1, 0},
            {1, 1, 1},
            {0, 0, 0}
    }, Color.MAGENTA, 1, 1),

    Z(new int[][]{
            {0, 0, 0},
            {1, 1, 0},
            {0, 1, 1}
    }, Color.RED, 1, 1);

    public final int [][] shape;
    public final Color color;
    public final int pivotX;
    public final int pivotY;


    public static TetrominoType getRandomTetrominoType() {
        TetrominoType[] types = TetrominoType.values();
        int randomIndex = (int) (Math.random() * types.length);
        return types[randomIndex];
    }
}
