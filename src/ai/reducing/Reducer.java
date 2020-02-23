package ai.reducing;

import board.Board;
import board.moves.Move;

public interface Reducer {

    /**
     * returns the amount of plys the move should be reduced
     * @param board
     * @param move
     * @param depth
     * @param depthLeft
     * @param moveIndex
     * @param pv_node
     * @return
     */
    public int reduce(Board board, Move move, int depth, int depthLeft, int moveIndex, boolean pv_node);
}
