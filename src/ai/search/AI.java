package ai.search;

import board.Board;
import board.moves.Move;
import game.Player;

/**
 * The AI interface is a sub-interface of Player and
 * is used for any class that can calculate the 'best move'
 * for a given Board(position).
 * When giving the Game class an AI, the AI move will
 * automatically be processed after the human has moved.
 * Things like Search-Depth are implemented in the subclasses.
 */
public interface AI extends Player {

    /**
     * A method that returns the best move for a given Board position.
     * @param board     the current board that stores information about pieces etc.
     * @return          the best move
     */
    Move bestMove(Board board);


}
