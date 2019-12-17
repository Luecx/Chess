package game.ai.ordering;

import board.moves.Move;
import game.ai.tools.PVLine;

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
     * @param lastIteration     the principal variation line from the last iteration
     * @param <T>               type-parameter for the move object.
     */
    <T extends Move> void sort(List<T> collection, int depth, PVLine lastIteration);

}
