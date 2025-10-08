package seoultech.se.core.command;

import seoultech.se.core.model.Board;

public interface GameCommand {
    void execute(Board board);
    CommandType getType();
}
