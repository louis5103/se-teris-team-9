package seoultech.se.core.command.implement.moveCommand;

import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.model.board.Board;

public class MoveLeftCommand implements GameCommand {
    @Override
    public void execute(Board board) {
        board.moveLeft();
    }

    @Override
    public CommandType getType() {
        return CommandType.MOVE_LEFT;
    }
}
