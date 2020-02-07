package ai.ordering;

import board.Board;
import board.moves.Move;
import ai.tools.KillerTable;
import ai.tools.PVLine;
import ai.tools.TranspositionTable;

import java.util.List;

public class NoOrderer implements Orderer {


    @Override
    public void sort(List<Move> collection, int depth, PVLine lastIteration, Board board, boolean pvNode,  KillerTable killerTable,
                                      TranspositionTable transpositionTable) {
    }
}
