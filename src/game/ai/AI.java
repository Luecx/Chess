package game.ai;

import board.Board;
import board.Move;
import game.Player;

public interface AI extends Player {

    Move bestMove(Board board);


}
