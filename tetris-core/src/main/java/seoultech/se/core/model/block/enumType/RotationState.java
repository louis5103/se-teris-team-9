package seoultech.se.core.model.block.enumType;

public enum RotationState {
    SPAWN,
    RIGHT,
    REVERSE,
    LEFT;

    public RotationState rotateClockwise() {
        return switch (this) {
            case SPAWN -> RIGHT;
            case RIGHT -> REVERSE;
            case REVERSE -> LEFT;
            case LEFT -> SPAWN;
        };
    }
    
    public RotationState rotateCounterClockwise() {
        return switch (this) {
            case SPAWN -> LEFT;
            case RIGHT -> SPAWN;
            case REVERSE -> RIGHT;
            case LEFT -> REVERSE;
        };
    }
}
