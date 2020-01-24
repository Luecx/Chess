package game.ai.ordering;

import board.Board;
import board.moves.Move;
import game.ai.tools.KillerTable;
import game.ai.tools.PVLine;
import game.ai.tools.TranspositionTable;

import java.util.List;

/**
 * this interface is used to sort moves in search algorithms.
 *
 */
public interface Orderer {

    /**
     * the method is used to sort moves in search algorithms.
     * It gets a list of the available moves which should be sorted.
     * Furthermore it gets the current depth.
     * A PVLine is given which is the principal variation line from the last iteration.
     * It could be null if there was no previous iteration or iterative deepening
     * is not used.
     *
     * @param collection        the moves to be sorted.
     * @param depth             the current depth. (depth increases)
     * @param lastIteration     [can be NULL] the principal variation line from the last iteration
     * @param board             the board object
     * @param killerTable       [can be NULL] a killerTable storing killer moves.

     */
    void sort(
            List<Move> collection,
            int depth,
            PVLine lastIteration,
            Board board,
            boolean pvNode,
            KillerTable killerTable,
            TranspositionTable transpositionTable);

}
