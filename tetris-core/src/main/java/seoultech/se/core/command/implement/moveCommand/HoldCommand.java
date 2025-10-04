package seoultech.se.core.command.implement.moveCommand;

import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.model.board.Board;

public class HoldCommand implements GameCommand {
    @Override
    public void execute(Board board) {
        board.hold();
    }

    @Override
    public CommandType getType() {
        return CommandType.HOLD;
    }

}
