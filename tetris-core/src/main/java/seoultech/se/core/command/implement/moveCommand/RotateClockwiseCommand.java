package seoultech.se.core.command.implement.moveCommand;

import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.model.board.Board;

/**
 * 시계방향 회전 커맨드
 */
public class RotateClockwiseCommand implements GameCommand {
    @Override
    public void execute(Board board) {
        board.rotateClockwise();
    }

    @Override
    public CommandType getType() {
        return CommandType.ROTATE_CLOCKWISE;
    }
}
