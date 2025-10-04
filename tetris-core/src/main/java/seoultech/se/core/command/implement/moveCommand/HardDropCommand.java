package seoultech.se.core.command.implement.moveCommand;

import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.model.board.Board;

public class HardDropCommand implements GameCommand {
    @Override
    public void execute(Board board) {
        board.hardDrop();
    }

    @Override
    public CommandType getType() {
        return CommandType.HARD_DROP;
    }

}
