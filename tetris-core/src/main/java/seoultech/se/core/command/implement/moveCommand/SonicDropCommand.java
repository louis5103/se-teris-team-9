package seoultech.se.core.command.implement.moveCommand;

import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;

public class SonicDropCommand implements GameCommand {
    @Override
    public void execute(seoultech.se.core.model.board.Board board) {
        // TODO: Board에 sonicDrop 메서드 구현 필요
    }

    @Override
    public CommandType getType() {
        return CommandType.SONIC_DROP;
    }

}
