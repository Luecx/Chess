package ai.ordering;

import ai.tools.tables.CounterMoveTable;
import ai.tools.tables.KillerTable;
import ai.tools.transpositions.TranspositionTable;
import board.Board;
import board.moves.Move;

import java.util.List;

public class NoOrderer implements Orderer {


    @Override
    public void sort(List<Move> collection, int depth, Board board, boolean pvNode,  KillerTable killerTable,
                                      TranspositionTable transpositionTable,
                     CounterMoveTable counterMoveTable) {
    }
}
