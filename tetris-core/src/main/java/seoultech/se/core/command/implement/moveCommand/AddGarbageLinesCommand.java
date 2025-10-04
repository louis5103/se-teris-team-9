package seoultech.se.core.command.implement.moveCommand;

import lombok.Getter;
import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;

@Getter
public class AddGarbageLinesCommand implements GameCommand {
    private final int lines;
    private final String sourcePlayerId;

    public AddGarbageLinesCommand(int lines, String sourcePlayerId) {
        this.lines = lines;
        this.sourcePlayerId = sourcePlayerId;
    }

    @Override
    public void execute(seoultech.se.core.model.board.Board board) {
        //TODO: implement
    }

    @Override
    public CommandType getType() {
        return CommandType.ADD_GARBAGE_LINES;
    }
}
