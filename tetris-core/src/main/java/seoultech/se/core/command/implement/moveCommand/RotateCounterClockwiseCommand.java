package seoultech.se.core.command.implement.moveCommand;

import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.model.board.Board;

/**
 * 반시계방향 회전 커맨드
 */
public class RotateCounterClockwiseCommand implements GameCommand {
    @Override
    public void execute(Board board) {
        board.rotateCounterClockwise();
    }

    @Override
    public CommandType getType() {
        return CommandType.ROTATE_COUNTER_CLOCKWISE;
    }
}
