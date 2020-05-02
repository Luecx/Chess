package ai.ordering;

import ai.tools.tables.CounterMoveTable;
import ai.tools.tables.KillerTable;
import ai.tools.transpositions.TranspositionTable;
import board.Board;
import board.moves.Move;

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
     * A PVLine is given which is the principal variation line from the last iterationGradient.
     * It could be null if there was no previous iterationGradient or iterative deepening
     * is not used.
     *
     * @param collection            the moves to be sorted.
     * @param depth                 the current depth. (depth increases)
     * @param board                 the board object
     * @param killerTable           [can be NULL] a killerTable storing killer moves.
     * @param killerTable           [can be NULL] a historyTable for the history heuristic.
     * @param transpositionTable    [can be null] a tt that stores (best) moves
     * @param pvNode                true if the current node is a pv node (leftmost)
     *
     */
    void sort(
            List<Move>                  collection,
            int                         depth,
            Board                       board,
            boolean                     pvNode,
            KillerTable                 killerTable,
            TranspositionTable          transpositionTable,
            CounterMoveTable            counterMoveTable);





}
