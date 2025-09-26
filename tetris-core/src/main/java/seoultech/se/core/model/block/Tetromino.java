package seoultech.se.core.model.block;

import lombok.Getter;
import seoultech.se.core.model.block.enumType.Color;
import seoultech.se.core.model.block.enumType.RotationState;
import seoultech.se.core.model.block.enumType.TetrominoType;

@Getter
public class Tetromino {
    private final TetrominoType type;
    private int[][] currentShape;
    private RotationState rotationState;

    // Constructor
    public Tetromino(TetrominoType type) {
        this.type = type;
        this.rotationState = RotationState.SPAWN;

        this.currentShape = new int[type.shape.length][];
        for (int i = 0; i < type.shape.length; i++) {
            currentShape[i] = new int[type.shape[i].length];
            System.arraycopy(type.shape[i], 0, currentShape[i], 0, type.shape[i].length);
        }
    }

    // Method to rotate the tetromino clockwise
    @Deprecated
    public void rotate() {
        if (type == TetrominoType.O) return ;

        int size = this.currentShape.length;
        int[][] rotatedShape = new int[size][size];
        for (int row=0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                rotatedShape[col][size - 1 - row] = this.currentShape[row][col];
            }
        }
        this.currentShape = rotatedShape;
        this.rotationState = rotationState.rotateClockwise();
    }

    // Method to get a new Tetromino instance with rotated shape
    // TODO: RotationState 타입에 따라 회전 구현.
    public Tetromino getRotatedInstance() {
        if (this.type == TetrominoType.O) return this;

        Tetromino rotatedTetromino = new Tetromino(this.type);
        int size = this.currentShape.length;
        int[][] rotatedShape = new int[size][size];
        for (int row=0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                rotatedShape[col][size - 1 - row] = this.currentShape[row][col];
            }
        }
        rotatedTetromino.currentShape = rotatedShape;
        rotatedTetromino.rotationState = this.rotationState.rotateClockwise();

        return rotatedTetromino;
    }

    // Method to rotate the tetromino counter-clockwise
    public Color getColor() { return type.color; }
    public int getPivotX() { return type.pivotX; }
    public int getPivotY() { return type.pivotY; }
}
