package seoultech.se.core.command.implement.moveCommand;

import lombok.Getter;
import seoultech.se.core.command.CommandType;
import seoultech.se.core.command.GameCommand;
import seoultech.se.core.model.block.enumType.TetrominoType;
import seoultech.se.core.model.board.Board;

@Getter
public class SpawnTetrominoCommand implements GameCommand {
    private final TetrominoType tetrominoType;

    public SpawnTetrominoCommand(TetrominoType tetrominoType) {
        this.tetrominoType = tetrominoType;
    }

    @Override
    public void execute(Board board) {
        // TODO: implement
    }

    @Override
    public CommandType getType() {
        return CommandType.SPAWN_TETROMINO;
    }

}
