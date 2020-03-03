package ai.ordering;

import ai.tools.tables.HistoryTable;
import board.Board;
import board.moves.Move;
import ai.tools.tables.KillerTable;
import ai.tools.PVLine;
import ai.tools.transpositions.TranspositionTable;

import java.util.List;

public class NoOrderer implements Orderer {


    @Override
    public void sort(List<Move> collection, int depth, PVLine lastIteration, Board board, boolean pvNode,  KillerTable killerTable,
                     HistoryTable historyTable,
                                      TranspositionTable transpositionTable) {
    }
}
